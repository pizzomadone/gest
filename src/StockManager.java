import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Centralized stock management class
 * Handles stock availability checks, reservations, and movements
 */
public class StockManager {

    /**
     * Check if there is sufficient stock for products in an order/invoice
     *
     * @param items List of items to check (product ID, quantity pairs)
     * @param existingDocumentId If editing, ID of existing document to exclude from check
     * @param documentType Type of document ("ORDER" or "INVOICE")
     * @return Map of product names to insufficient quantities, empty if all OK
     */
    public static Map<String, StockAvailability> checkStockAvailability(
            Connection conn,
            List<StockItem> items,
            Integer existingDocumentId,
            String documentType) throws SQLException {

        Map<String, StockAvailability> insufficientProducts = new HashMap<>();

        for (StockItem item : items) {
            int productId = item.getProductId();
            String productName = item.getProductName();
            int requestedQty = item.getQuantity();

            // Get current stock and reserved quantity from database
            String query = """
                SELECT quantita, quantita_riservata
                FROM prodotti
                WHERE id = ?
            """;

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, productId);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    int currentStock = rs.getInt("quantita");
                    int reservedStock = rs.getInt("quantita_riservata");
                    int availableStock = currentStock - reservedStock;

                    // If editing existing document, add back the old quantity for this product
                    if (existingDocumentId != null) {
                        int oldQty = getOldProductQuantity(conn, existingDocumentId, productId, documentType);
                        availableStock += oldQty;
                    }

                    if (requestedQty > availableStock) {
                        insufficientProducts.put(productName, new StockAvailability(
                            currentStock, reservedStock, availableStock, requestedQty
                        ));
                    }
                }
            }
        }

        return insufficientProducts;
    }

    /**
     * Get the old quantity of a product in an existing document
     */
    private static int getOldProductQuantity(Connection conn, int documentId, int productId, String documentType) throws SQLException {
        String tableName = documentType.equals("ORDER") ? "dettagli_ordine" : "dettagli_fattura";
        String columnName = documentType.equals("ORDER") ? "ordine_id" : "fattura_id";

        String query = String.format(
            "SELECT quantita FROM %s WHERE %s = ? AND prodotto_id = ?",
            tableName, columnName
        );

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, documentId);
            pstmt.setInt(2, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("quantita");
            }
        }
        return 0;
    }

    /**
     * Create or update a stock reservation
     */
    public static void createOrUpdateReservation(
            Connection conn,
            int productId,
            String documentType,
            int documentId,
            int quantity,
            String note) throws SQLException {

        // Check if reservation already exists
        String checkQuery = """
            SELECT id, quantita_riservata, stato
            FROM prenotazioni_stock
            WHERE prodotto_id = ? AND tipo_documento = ? AND documento_id = ?
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
            pstmt.setInt(1, productId);
            pstmt.setString(2, documentType);
            pstmt.setInt(3, documentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Update existing reservation
                int reservationId = rs.getInt("id");
                String updateQuery = """
                    UPDATE prenotazioni_stock
                    SET quantita_riservata = ?, stato = 'ACTIVE', note = ?
                    WHERE id = ?
                """;

                try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                    updateStmt.setInt(1, quantity);
                    updateStmt.setString(2, note);
                    updateStmt.setInt(3, reservationId);
                    updateStmt.executeUpdate();
                }
            } else {
                // Create new reservation
                String insertQuery = """
                    INSERT INTO prenotazioni_stock
                    (prodotto_id, tipo_documento, documento_id, quantita_riservata, stato, note)
                    VALUES (?, ?, ?, ?, 'ACTIVE', ?)
                """;

                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    insertStmt.setInt(1, productId);
                    insertStmt.setString(2, documentType);
                    insertStmt.setInt(3, documentId);
                    insertStmt.setInt(4, quantity);
                    insertStmt.setString(5, note);
                    insertStmt.executeUpdate();
                }
            }
        }
    }

    /**
     * Cancel a stock reservation (mark as CANCELLED)
     */
    public static void cancelReservation(
            Connection conn,
            String documentType,
            int documentId) throws SQLException {

        String updateQuery = """
            UPDATE prenotazioni_stock
            SET stato = 'CANCELLED'
            WHERE tipo_documento = ? AND documento_id = ? AND stato = 'ACTIVE'
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            pstmt.setString(1, documentType);
            pstmt.setInt(2, documentId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Complete a stock reservation and decrement actual stock
     * This converts reserved stock into actual stock decrease
     */
    public static void completeReservationAndDecrementStock(
            Connection conn,
            String documentType,
            int documentId,
            Date documentDate,
            String documentNumber) throws SQLException {

        // Get all active reservations for this document
        String getReservationsQuery = """
            SELECT prodotto_id, quantita_riservata
            FROM prenotazioni_stock
            WHERE tipo_documento = ? AND documento_id = ? AND stato = 'ACTIVE'
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(getReservationsQuery)) {
            pstmt.setString(1, documentType);
            pstmt.setInt(2, documentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int productId = rs.getInt("prodotto_id");
                int quantity = rs.getInt("quantita_riservata");

                // Decrement actual stock
                decrementStock(conn, productId, quantity);

                // Create warehouse movement
                createWarehouseMovement(conn, productId, documentDate, quantity,
                    "SALE", documentNumber, documentType,
                    documentType + " " + documentNumber);
            }
        }

        // Mark reservations as COMPLETED
        String updateQuery = """
            UPDATE prenotazioni_stock
            SET stato = 'COMPLETED'
            WHERE tipo_documento = ? AND documento_id = ? AND stato = 'ACTIVE'
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            pstmt.setString(1, documentType);
            pstmt.setInt(2, documentId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Decrement stock directly (for invoices without prior reservation)
     */
    public static void decrementStockDirectly(
            Connection conn,
            List<StockItem> items,
            Date documentDate,
            String documentNumber,
            String documentType) throws SQLException {

        for (StockItem item : items) {
            // Decrement stock
            decrementStock(conn, item.getProductId(), item.getQuantity());

            // Create warehouse movement
            createWarehouseMovement(conn, item.getProductId(), documentDate,
                item.getQuantity(), "SALE", documentNumber, documentType,
                documentType + " " + documentNumber);
        }
    }

    /**
     * Increment stock (for supplier orders)
     */
    public static void incrementStock(
            Connection conn,
            List<StockItem> items,
            Date documentDate,
            String documentNumber,
            String documentType) throws SQLException {

        for (StockItem item : items) {
            // Increment stock
            String updateQuery = "UPDATE prodotti SET quantita = quantita + ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                pstmt.setInt(1, item.getQuantity());
                pstmt.setInt(2, item.getProductId());
                pstmt.executeUpdate();
            }

            // Create warehouse movement
            createWarehouseMovement(conn, item.getProductId(), documentDate,
                item.getQuantity(), "PURCHASE", documentNumber, documentType,
                "Supplier order " + documentNumber);
        }
    }

    /**
     * Restore stock from a previous document (when editing/deleting)
     */
    public static void restoreStockFromDocument(
            Connection conn,
            int documentId,
            String documentType) throws SQLException {

        String tableName = documentType.equals("ORDER") ? "dettagli_ordine" : "dettagli_fattura";
        String columnName = documentType.equals("ORDER") ? "ordine_id" : "fattura_id";

        String query = String.format(
            "SELECT prodotto_id, quantita FROM %s WHERE %s = ?",
            tableName, columnName
        );

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, documentId);
            ResultSet rs = pstmt.executeQuery();

            String updateQuery = "UPDATE prodotti SET quantita = quantita + ? WHERE id = ?";
            try (PreparedStatement updatePstmt = conn.prepareStatement(updateQuery)) {
                while (rs.next()) {
                    int productId = rs.getInt("prodotto_id");
                    int quantity = rs.getInt("quantita");

                    updatePstmt.setInt(1, quantity);
                    updatePstmt.setInt(2, productId);
                    updatePstmt.executeUpdate();
                }
            }
        }
    }

    /**
     * Helper: Decrement stock quantity
     */
    private static void decrementStock(Connection conn, int productId, int quantity) throws SQLException {
        String updateQuery = "UPDATE prodotti SET quantita = quantita - ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, productId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Helper: Create warehouse movement record
     */
    private static void createWarehouseMovement(
            Connection conn,
            int productId,
            Date documentDate,
            int quantity,
            String reason,
            String documentNumber,
            String documentType,
            String note) throws SQLException {

        String movementQuery = """
            INSERT INTO movimenti_magazzino
            (prodotto_id, data, tipo, quantita, causale, documento_numero, documento_tipo, note)
            VALUES (?, ?, 'OUTWARD', ?, ?, ?, ?, ?)
        """;

        // INWARD for purchases, OUTWARD for sales
        String movementType = reason.equals("PURCHASE") ? "INWARD" : "OUTWARD";

        movementQuery = """
            INSERT INTO movimenti_magazzino
            (prodotto_id, data, tipo, quantita, causale, documento_numero, documento_tipo, note)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(movementQuery)) {
            pstmt.setInt(1, productId);
            pstmt.setTimestamp(2, DateUtils.toSqlTimestamp(documentDate));
            pstmt.setString(3, movementType);
            pstmt.setInt(4, quantity);
            pstmt.setString(5, reason);
            pstmt.setString(6, documentNumber);
            pstmt.setString(7, documentType);
            pstmt.setString(8, note);
            pstmt.executeUpdate();
        }
    }

    /**
     * Get available stock for a product (physical - reserved)
     */
    public static int getAvailableStock(Connection conn, int productId) throws SQLException {
        String query = """
            SELECT quantita, quantita_riservata
            FROM prodotti
            WHERE id = ?
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int stock = rs.getInt("quantita");
                int reserved = rs.getInt("quantita_riservata");
                return stock - reserved;
            }
        }
        return 0;
    }

    /**
     * Delete an order and restore stock based on its status
     * Handles: restoring stock if Completed, cancelling reservations if In Progress
     */
    public static void deleteOrder(Connection conn, int orderId, String orderStatus) throws SQLException {
        // If order was Completed, restore stock
        if ("Completed".equals(orderStatus)) {
            restoreStockFromDocument(conn, orderId, "ORDER");
        }

        // If order was In Progress, cancel active reservations
        if ("In Progress".equals(orderStatus)) {
            cancelReservation(conn, "ORDER", orderId);
        }

        // Delete warehouse movements related to this order
        deleteWarehouseMovements(conn, "ORDER", String.valueOf(orderId));
    }

    /**
     * Delete an invoice and restore stock based on its status
     */
    public static void deleteInvoice(Connection conn, int invoiceId, String invoiceNumber, String invoiceStatus) throws SQLException {
        // If invoice was Issued or Paid, restore stock
        if ("Issued".equals(invoiceStatus) || "Paid".equals(invoiceStatus)) {
            restoreStockFromDocument(conn, invoiceId, "INVOICE");
        }

        // Delete warehouse movements related to this invoice
        deleteWarehouseMovements(conn, "INVOICE", invoiceNumber);
    }

    /**
     * Delete warehouse movements for a specific document
     */
    public static void deleteWarehouseMovements(Connection conn, String documentType, String documentNumber) throws SQLException {
        String deleteQuery = """
            DELETE FROM movimenti_magazzino
            WHERE documento_tipo = ? AND documento_numero = ?
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {
            pstmt.setString(1, documentType);
            pstmt.setString(2, documentNumber);
            pstmt.executeUpdate();
        }
    }

    // Helper classes

    public static class StockItem {
        private int productId;
        private String productName;
        private int quantity;

        public StockItem(int productId, String productName, int quantity) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
        }

        public int getProductId() { return productId; }
        public String getProductName() { return productName; }
        public int getQuantity() { return quantity; }
    }

    public static class StockAvailability {
        private int physicalStock;
        private int reservedStock;
        private int availableStock;
        private int requestedQuantity;

        public StockAvailability(int physicalStock, int reservedStock,
                                int availableStock, int requestedQuantity) {
            this.physicalStock = physicalStock;
            this.reservedStock = reservedStock;
            this.availableStock = availableStock;
            this.requestedQuantity = requestedQuantity;
        }

        public int getPhysicalStock() { return physicalStock; }
        public int getReservedStock() { return reservedStock; }
        public int getAvailableStock() { return availableStock; }
        public int getRequestedQuantity() { return requestedQuantity; }

        public String getFormattedMessage() {
            return String.format(
                "Physical: %d, Reserved: %d, Available: %d, Requested: %d",
                physicalStock, reservedStock, availableStock, requestedQuantity
            );
        }
    }
}

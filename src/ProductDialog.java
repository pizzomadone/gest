import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ProductDialog extends JDialog {
    private JTextField codeField;
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JTextField priceField;
    private JSpinner quantitySpinner;

    // New fields
    private JComboBox<String> categoryComboBox;
    private JTextField alternativeSkuField;
    private JTextField weightField;
    private JComboBox<String> unitOfMeasureComboBox;
    private JSpinner minimumQuantitySpinner;
    private JTextField acquisitionCostField;
    private JCheckBox activeCheckBox;
    private JButton selectSupplierButton;
    private Supplier selectedSupplier;

    private boolean productSaved = false;
    private Product product;
    
    // Constructor for JFrame parent
    public ProductDialog(JFrame parent, Product product) {
        super(parent, product == null ? "New Product" : "Edit Product", true);
        this.product = product;
        
        setupDialog();
        initComponents();
        if (product != null) {
            loadProductData();
        }
    }
    
    // Constructor for JDialog parent
    public ProductDialog(JDialog parent, Product product) {
        super(parent, product == null ? "New Product" : "Edit Product", true);
        this.product = product;
        
        setupDialog();
        initComponents();
        if (product != null) {
            loadProductData();
        }
    }
    
    private void setupDialog() {
        setSize(650, 700);
        setLocationRelativeTo(null); // Center on screen instead of parent window
        setLayout(new BorderLayout(10, 10));
    }
    
    private void initComponents() {
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5); // Reduced vertical spacing for compactness
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Code
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Code:"), gbc);

        gbc.gridx = 1;
        codeField = new JTextField(12);
        formPanel.add(codeField, gbc);

        // Name
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Name:"), gbc);

        gbc.gridx = 1;
        nameField = new JTextField(12);
        formPanel.add(nameField, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Description:"), gbc);

        gbc.gridx = 1;
        descriptionArea = new JTextArea(3, 12);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(descriptionArea), gbc);

        // Price
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Price:"), gbc);

        gbc.gridx = 1;
        priceField = new JTextField(12);
        formPanel.add(priceField, gbc);

        // Quantity
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Quantity:"), gbc);

        gbc.gridx = 1;
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, 999999, 1);
        quantitySpinner = new JSpinner(spinnerModel);
        formPanel.add(quantitySpinner, gbc);

        // Category
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Category:"), gbc);

        gbc.gridx = 1;
        String[] categories = {"", "Electronics", "Furniture", "Clothing", "Food", "Tools", "Office Supplies", "Other"};
        categoryComboBox = new JComboBox<>(categories);
        categoryComboBox.setEditable(true);
        formPanel.add(categoryComboBox, gbc);

        // Alternative SKU
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Alternative SKU:"), gbc);

        gbc.gridx = 1;
        alternativeSkuField = new JTextField(12);
        formPanel.add(alternativeSkuField, gbc);

        // Weight
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Weight (kg):"), gbc);

        gbc.gridx = 1;
        weightField = new JTextField(12);
        weightField.setText("0.0");
        formPanel.add(weightField, gbc);

        // Unit of Measure
        gbc.gridx = 0; gbc.gridy = 8;
        formPanel.add(new JLabel("Unit of Measure:"), gbc);

        gbc.gridx = 1;
        String[] units = {"pz", "kg", "m", "litri", "box", "pack"};
        unitOfMeasureComboBox = new JComboBox<>(units);
        unitOfMeasureComboBox.setEditable(true);
        formPanel.add(unitOfMeasureComboBox, gbc);

        // Minimum Quantity
        gbc.gridx = 0; gbc.gridy = 9;
        formPanel.add(new JLabel("Minimum Quantity:"), gbc);

        gbc.gridx = 1;
        SpinnerNumberModel minQtyModel = new SpinnerNumberModel(0, 0, 999999, 1);
        minimumQuantitySpinner = new JSpinner(minQtyModel);
        formPanel.add(minimumQuantitySpinner, gbc);

        // Acquisition Cost
        gbc.gridx = 0; gbc.gridy = 10;
        formPanel.add(new JLabel("Acquisition Cost:"), gbc);

        gbc.gridx = 1;
        acquisitionCostField = new JTextField(12);
        acquisitionCostField.setText("0.0");
        formPanel.add(acquisitionCostField, gbc);

        // Active
        gbc.gridx = 0; gbc.gridy = 11;
        formPanel.add(new JLabel("Active:"), gbc);

        gbc.gridx = 1;
        activeCheckBox = new JCheckBox("", true);
        formPanel.add(activeCheckBox, gbc);

        // Supplier
        gbc.gridx = 0; gbc.gridy = 12;
        formPanel.add(new JLabel("Supplier:"), gbc);

        gbc.gridx = 1;
        selectSupplierButton = new JButton("Click to select supplier...");
        selectSupplierButton.setPreferredSize(new Dimension(250, 35));
        selectSupplierButton.setHorizontalAlignment(SwingConstants.LEFT);
        selectSupplierButton.addActionListener(e -> showSupplierSelectionDialog());
        formPanel.add(selectSupplierButton, gbc);

        gbc.gridx = 2;
        JButton newSupplierButton = new JButton("New Supplier");
        newSupplierButton.addActionListener(e -> createNewSupplier());
        formPanel.add(newSupplierButton, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> saveProduct());
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        // Main layout
        add(new JScrollPane(formPanel), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadProductData() {
        codeField.setText(product.getCode());
        nameField.setText(product.getName());
        descriptionArea.setText(product.getDescription());
        priceField.setText(String.valueOf(product.getPrice()));
        quantitySpinner.setValue(product.getQuantity());

        // Load new fields
        categoryComboBox.setSelectedItem(product.getCategory());
        alternativeSkuField.setText(product.getAlternativeSku());
        weightField.setText(String.valueOf(product.getWeight()));
        unitOfMeasureComboBox.setSelectedItem(product.getUnitOfMeasure());
        minimumQuantitySpinner.setValue(product.getMinimumQuantity());
        acquisitionCostField.setText(String.valueOf(product.getAcquisitionCost()));
        activeCheckBox.setSelected(product.isActive());

        // Load supplier if present
        Integer supplierId = product.getSupplierId();
        if (supplierId != null && supplierId > 0) {
            loadSupplierById(supplierId);
        }
    }

    private void loadSupplierById(Integer supplierId) {
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            String query = "SELECT * FROM suppliers WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, supplierId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        selectedSupplier = new Supplier(
                            rs.getInt("id"),
                            rs.getString("ragione_sociale"),
                            rs.getString("partita_iva"),
                            rs.getString("codice_fiscale"),
                            rs.getString("indirizzo"),
                            rs.getString("telefono"),
                            rs.getString("email"),
                            rs.getString("pec"),
                            rs.getString("sito_web"),
                            rs.getString("note")
                        );
                        updateSupplierButton();
                    }
                }
            }
        } catch (SQLException e) {
            // If supplier not found, clear the button
            selectSupplierButton.setText("Click to select supplier...");
        }
    }

    private void showSupplierSelectionDialog() {
        SupplierSelectionDialog dialog = new SupplierSelectionDialog(this);
        dialog.setVisible(true);

        if (dialog.isSupplierSelected()) {
            selectedSupplier = dialog.getSelectedSupplier();
            updateSupplierButton();
        }
    }

    private void updateSupplierButton() {
        if (selectedSupplier != null) {
            String buttonText = selectedSupplier.getRagioneSociale();

            if (selectedSupplier.getPartitaIva() != null && !selectedSupplier.getPartitaIva().isEmpty()) {
                buttonText += " (P.IVA: " + selectedSupplier.getPartitaIva() + ")";
            }

            selectSupplierButton.setText(buttonText);
            selectSupplierButton.setToolTipText("Supplier: " + buttonText);
        }
    }

    private void createNewSupplier() {
        SupplierDialog dialog = new SupplierDialog(this, null);
        dialog.setVisible(true);
        if (dialog.isSupplierSaved()) {
            JOptionPane.showMessageDialog(this,
                "Supplier created successfully. Please select it from the list.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void saveProduct() {
        try {
            // Validation
            String code = codeField.getText().trim();
            String name = nameField.getText().trim();
            String description = descriptionArea.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());
            int quantity = (int)quantitySpinner.getValue();

            // New fields
            String category = (String)categoryComboBox.getSelectedItem();
            String alternativeSku = alternativeSkuField.getText().trim();
            double weight = Double.parseDouble(weightField.getText().trim());
            String unitOfMeasure = (String)unitOfMeasureComboBox.getSelectedItem();
            int minimumQuantity = (int)minimumQuantitySpinner.getValue();
            double acquisitionCost = Double.parseDouble(acquisitionCostField.getText().trim());
            boolean active = activeCheckBox.isSelected();
            Integer supplierId = selectedSupplier != null ? selectedSupplier.getId() : null;

            if (code.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Code and Name fields are required",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Connection conn = DatabaseManager.getInstance().getConnection();
            if (product == null) { // New product
                String query = """
                    INSERT INTO products (code, name, description, price, quantity,
                        category, alternative_sku, weight, unit_of_measure, minimum_quantity,
                        acquisition_cost, active, supplier_id)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, code);
                    pstmt.setString(2, name);
                    pstmt.setString(3, description);
                    pstmt.setDouble(4, price);
                    pstmt.setInt(5, quantity);
                    pstmt.setString(6, category);
                    pstmt.setString(7, alternativeSku);
                    pstmt.setDouble(8, weight);
                    pstmt.setString(9, unitOfMeasure);
                    pstmt.setInt(10, minimumQuantity);
                    pstmt.setDouble(11, acquisitionCost);
                    pstmt.setInt(12, active ? 1 : 0);
                    if (supplierId != null) {
                        pstmt.setInt(13, supplierId);
                    } else {
                        pstmt.setNull(13, java.sql.Types.INTEGER);
                    }
                    pstmt.executeUpdate();
                }
            } else { // Edit product
                String query = """
                    UPDATE products
                    SET code = ?, name = ?, description = ?, price = ?, quantity = ?,
                        category = ?, alternative_sku = ?, weight = ?, unit_of_measure = ?,
                        minimum_quantity = ?, acquisition_cost = ?, active = ?, supplier_id = ?
                    WHERE id = ?
                """;
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, code);
                    pstmt.setString(2, name);
                    pstmt.setString(3, description);
                    pstmt.setDouble(4, price);
                    pstmt.setInt(5, quantity);
                    pstmt.setString(6, category);
                    pstmt.setString(7, alternativeSku);
                    pstmt.setDouble(8, weight);
                    pstmt.setString(9, unitOfMeasure);
                    pstmt.setInt(10, minimumQuantity);
                    pstmt.setDouble(11, acquisitionCost);
                    pstmt.setInt(12, active ? 1 : 0);
                    if (supplierId != null) {
                        pstmt.setInt(13, supplierId);
                    } else {
                        pstmt.setNull(13, java.sql.Types.INTEGER);
                    }
                    pstmt.setInt(14, product.getId());
                    pstmt.executeUpdate();
                }
            }

            productSaved = true;
            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Price, Weight, and Acquisition Cost must be valid numbers",
                "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error while saving product: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isProductSaved() {
        return productSaved;
    }
}
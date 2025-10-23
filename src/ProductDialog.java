import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ProductDialog extends JDialog {
    private JTextField codiceField;
    private JTextField nomeField;
    private JTextArea descrizioneArea;
    private JTextField prezzoField;
    private JSpinner quantitaSpinner;

    // New fields
    private JComboBox<String> categoryComboBox;
    private JTextField alternativeSkuField;
    private JTextField weightField;
    private JComboBox<String> unitOfMeasureComboBox;
    private JSpinner minimumQuantitySpinner;
    private JTextField acquisitionCostField;
    private JCheckBox activeCheckBox;
    private JTextField supplierField;

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
        setSize(600, 700);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(10, 10));
    }
    
    private void initComponents() {
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Code
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Code:"), gbc);
        
        gbc.gridx = 1;
        codiceField = new JTextField(20);
        formPanel.add(codiceField, gbc);
        
        // Name
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Name:"), gbc);
        
        gbc.gridx = 1;
        nomeField = new JTextField(20);
        formPanel.add(nomeField, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1;
        descrizioneArea = new JTextArea(4, 20);
        descrizioneArea.setLineWrap(true);
        descrizioneArea.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(descrizioneArea), gbc);
        
        // Price
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Price:"), gbc);
        
        gbc.gridx = 1;
        prezzoField = new JTextField(20);
        formPanel.add(prezzoField, gbc);
        
        // Quantity
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Quantity:"), gbc);

        gbc.gridx = 1;
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, 999999, 1);
        quantitaSpinner = new JSpinner(spinnerModel);
        formPanel.add(quantitaSpinner, gbc);

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
        alternativeSkuField = new JTextField(20);
        formPanel.add(alternativeSkuField, gbc);

        // Weight
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Weight (kg):"), gbc);

        gbc.gridx = 1;
        weightField = new JTextField(20);
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
        acquisitionCostField = new JTextField(20);
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
        supplierField = new JTextField(20);
        formPanel.add(supplierField, gbc);

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
        codiceField.setText(product.getCodice());
        nomeField.setText(product.getNome());
        descrizioneArea.setText(product.getDescrizione());
        prezzoField.setText(String.valueOf(product.getPrezzo()));
        quantitaSpinner.setValue(product.getQuantita());

        // Load new fields
        categoryComboBox.setSelectedItem(product.getCategory());
        alternativeSkuField.setText(product.getAlternativeSku());
        weightField.setText(String.valueOf(product.getWeight()));
        unitOfMeasureComboBox.setSelectedItem(product.getUnitOfMeasure());
        minimumQuantitySpinner.setValue(product.getMinimumQuantity());
        acquisitionCostField.setText(String.valueOf(product.getAcquisitionCost()));
        activeCheckBox.setSelected(product.isActive());
        supplierField.setText(product.getSupplier());
    }
    
    private void saveProduct() {
        try {
            // Validation
            String codice = codiceField.getText().trim();
            String nome = nomeField.getText().trim();
            String descrizione = descrizioneArea.getText().trim();
            double prezzo = Double.parseDouble(prezzoField.getText().trim());
            int quantita = (int)quantitaSpinner.getValue();

            // New fields
            String category = (String)categoryComboBox.getSelectedItem();
            String alternativeSku = alternativeSkuField.getText().trim();
            double weight = Double.parseDouble(weightField.getText().trim());
            String unitOfMeasure = (String)unitOfMeasureComboBox.getSelectedItem();
            int minimumQuantity = (int)minimumQuantitySpinner.getValue();
            double acquisitionCost = Double.parseDouble(acquisitionCostField.getText().trim());
            boolean active = activeCheckBox.isSelected();
            String supplier = supplierField.getText().trim();

            if (codice.isEmpty() || nome.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Code and Name fields are required",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Connection conn = DatabaseManager.getInstance().getConnection();
            if (product == null) { // New product
                String query = """
                    INSERT INTO prodotti (codice, nome, descrizione, prezzo, quantita,
                        category, alternative_sku, weight, unit_of_measure, minimum_quantity,
                        acquisition_cost, active, supplier)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, codice);
                    pstmt.setString(2, nome);
                    pstmt.setString(3, descrizione);
                    pstmt.setDouble(4, prezzo);
                    pstmt.setInt(5, quantita);
                    pstmt.setString(6, category);
                    pstmt.setString(7, alternativeSku);
                    pstmt.setDouble(8, weight);
                    pstmt.setString(9, unitOfMeasure);
                    pstmt.setInt(10, minimumQuantity);
                    pstmt.setDouble(11, acquisitionCost);
                    pstmt.setInt(12, active ? 1 : 0);
                    pstmt.setString(13, supplier);
                    pstmt.executeUpdate();
                }
            } else { // Edit product
                String query = """
                    UPDATE prodotti
                    SET codice = ?, nome = ?, descrizione = ?, prezzo = ?, quantita = ?,
                        category = ?, alternative_sku = ?, weight = ?, unit_of_measure = ?,
                        minimum_quantity = ?, acquisition_cost = ?, active = ?, supplier = ?
                    WHERE id = ?
                """;
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, codice);
                    pstmt.setString(2, nome);
                    pstmt.setString(3, descrizione);
                    pstmt.setDouble(4, prezzo);
                    pstmt.setInt(5, quantita);
                    pstmt.setString(6, category);
                    pstmt.setString(7, alternativeSku);
                    pstmt.setDouble(8, weight);
                    pstmt.setString(9, unitOfMeasure);
                    pstmt.setInt(10, minimumQuantity);
                    pstmt.setDouble(11, acquisitionCost);
                    pstmt.setInt(12, active ? 1 : 0);
                    pstmt.setString(13, supplier);
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
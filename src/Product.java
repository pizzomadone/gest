// File: Product.java
public class Product {
    private int id;
    private String codice;
    private String nome;
    private String descrizione;
    private double prezzo;
    private int quantita;

    // Category & Management
    private String category;
    private String alternativeSku;

    // Dimensions & Weight
    private double weight;
    private String unitOfMeasure;

    // Stock Management
    private int minimumQuantity;
    private double acquisitionCost;

    // Logistics
    private boolean active;
    private String supplier;

    public Product(int id, String codice, String nome, String descrizione, double prezzo, int quantita) {
        this.id = id;
        this.codice = codice;
        this.nome = nome;
        this.descrizione = descrizione;
        this.prezzo = prezzo;
        this.quantita = quantita;
        this.category = "";
        this.alternativeSku = "";
        this.weight = 0.0;
        this.unitOfMeasure = "pz";
        this.minimumQuantity = 0;
        this.acquisitionCost = 0.0;
        this.active = true;
        this.supplier = "";
    }

    public Product(int id, String codice, String nome, String descrizione, double prezzo, int quantita,
                   String category, String alternativeSku, double weight, String unitOfMeasure,
                   int minimumQuantity, double acquisitionCost, boolean active, String supplier) {
        this.id = id;
        this.codice = codice;
        this.nome = nome;
        this.descrizione = descrizione;
        this.prezzo = prezzo;
        this.quantita = quantita;
        this.category = category;
        this.alternativeSku = alternativeSku;
        this.weight = weight;
        this.unitOfMeasure = unitOfMeasure;
        this.minimumQuantity = minimumQuantity;
        this.acquisitionCost = acquisitionCost;
        this.active = active;
        this.supplier = supplier;
    }

    // Getters
    public int getId() { return id; }
    public String getCodice() { return codice; }
    public String getNome() { return nome; }
    public String getDescrizione() { return descrizione; }
    public double getPrezzo() { return prezzo; }
    public int getQuantita() { return quantita; }
    public String getCategory() { return category; }
    public String getAlternativeSku() { return alternativeSku; }
    public double getWeight() { return weight; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public int getMinimumQuantity() { return minimumQuantity; }
    public double getAcquisitionCost() { return acquisitionCost; }
    public boolean isActive() { return active; }
    public String getSupplier() { return supplier; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setCodice(String codice) { this.codice = codice; }
    public void setNome(String nome) { this.nome = nome; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }
    public void setPrezzo(double prezzo) { this.prezzo = prezzo; }
    public void setQuantita(int quantita) { this.quantita = quantita; }
    public void setCategory(String category) { this.category = category; }
    public void setAlternativeSku(String alternativeSku) { this.alternativeSku = alternativeSku; }
    public void setWeight(double weight) { this.weight = weight; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }
    public void setMinimumQuantity(int minimumQuantity) { this.minimumQuantity = minimumQuantity; }
    public void setAcquisitionCost(double acquisitionCost) { this.acquisitionCost = acquisitionCost; }
    public void setActive(boolean active) { this.active = active; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
}
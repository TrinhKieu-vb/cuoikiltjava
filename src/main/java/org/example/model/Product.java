package org.example.model;

import java.io.Serializable;

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private double price;
    private int categoryId;
    private String categoryName;
    private String description;
    private int stock;
    private long createdAt;
    private String imagePath;

    public Product() {}

    public Product(int id, String name, double price, int categoryId, String description, int stock) {
        this(id, name, price, categoryId, description, stock, null);
    }

    public Product(int id, String name, double price, int categoryId, String description, int stock, String imagePath) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.categoryId = categoryId;
        this.description = description;
        this.stock = stock;
        this.imagePath = imagePath;
        this.createdAt = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category=" + categoryName +
                ", imagePath=" + imagePath +
                '}';
    }
}


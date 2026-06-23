package org.example.dao;

import org.example.database.DatabaseUtil;
import org.example.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    private static final Logger logger = LoggerFactory.getLogger(ProductDAO.class);

    public ProductDAO() {
        checkAndCreateImagePathColumn();
    }

    private void checkAndCreateImagePathColumn() {
        String checkSql = "SHOW COLUMNS FROM products LIKE 'image_path'";
        String alterSql = "ALTER TABLE products ADD COLUMN image_path VARCHAR(255) DEFAULT NULL";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(checkSql);
            if (!rs.next()) {
                logger.info("Column 'image_path' does not exist in 'products' table. Creating it...");
                stmt.executeUpdate(alterSql);
                logger.info("Column 'image_path' created successfully.");
                
                // Set default images for sample products
                String updateSampleImages = 
                    "UPDATE products SET image_path = CASE id " +
                    "WHEN 1 THEN 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=150' " +
                    "WHEN 2 THEN 'https://images.unsplash.com/photo-1541167760496-1628856ab772?w=150' " +
                    "WHEN 3 THEN 'https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=150' " +
                    "WHEN 4 THEN 'https://images.unsplash.com/photo-1536256263959-770b48d82b0a?w=150' " +
                    "WHEN 5 THEN 'https://images.unsplash.com/photo-1488477181946-6428a0291777?w=150' " +
                    "WHEN 6 THEN 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=150' " +
                    "WHEN 7 THEN 'https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=150' " +
                    "WHEN 8 THEN 'https://images.unsplash.com/photo-1509440159596-0249088772ff?w=150' " +
                    "ELSE NULL END";
                stmt.executeUpdate(updateSampleImages);
            }
        } catch (SQLException e) {
            logger.error("Error checking or creating 'image_path' column", e);
        }
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.name as category_name FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id ORDER BY p.name";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
            logger.info("Retrieved " + products.size() + " products");
        } catch (SQLException e) {
            logger.error("Error retrieving products", e);
        }
        return products;
    }

    public Product getProductById(int id) {
        String sql = "SELECT p.*, c.name as category_name FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id WHERE p.id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving product by id", e);
        }
        return null;
    }

    public List<Product> searchProducts(String keyword) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.name as category_name FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id " +
                     "WHERE p.name LIKE ? OR p.description LIKE ? ORDER BY p.name";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String searchTerm = "%" + keyword + "%";
            ps.setString(1, searchTerm);
            ps.setString(2, searchTerm);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
            logger.info("Search found " + products.size() + " products for keyword: " + keyword);
        } catch (SQLException e) {
            logger.error("Error searching products", e);
        }
        return products;
    }

    public List<Product> getProductsByCategory(int categoryId) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.name as category_name FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id " +
                     "WHERE p.category_id = ? ORDER BY p.name";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving products by category", e);
        }
        return products;
    }

    public boolean createProduct(Product product) {
        String sql = "INSERT INTO products (name, price, category_id, description, stock, created_at, image_path) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setDouble(2, product.getPrice());
            ps.setInt(3, product.getCategoryId());
            ps.setString(4, product.getDescription());
            ps.setInt(5, product.getStock());
            ps.setLong(6, System.currentTimeMillis());
            ps.setString(7, product.getImagePath());
            int affectedRows = ps.executeUpdate();
            logger.info("Product created: " + product.getName());
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error creating product", e);
        }
        return false;
    }

    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET name = ?, price = ?, category_id = ?, description = ?, stock = ?, image_path = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setDouble(2, product.getPrice());
            ps.setInt(3, product.getCategoryId());
            ps.setString(4, product.getDescription());
            ps.setInt(5, product.getStock());
            ps.setString(6, product.getImagePath());
            ps.setInt(7, product.getId());
            int affectedRows = ps.executeUpdate();
            logger.info("Product updated: " + product.getName());
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error updating product", e);
        }
        return false;
    }

    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affectedRows = ps.executeUpdate();
            logger.info("Product deleted with id: " + id);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error deleting product", e);
        }
        return false;
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getDouble("price"),
            rs.getInt("category_id"),
            rs.getString("description"),
            rs.getInt("stock"),
            rs.getString("image_path")
        );
        product.setCategoryName(rs.getString("category_name"));
        product.setCreatedAt(rs.getLong("created_at"));
        return product;
    }
}


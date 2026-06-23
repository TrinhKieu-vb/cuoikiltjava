package org.example.dao;

import org.example.database.DatabaseUtil;
import org.example.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    private static final Logger logger = LoggerFactory.getLogger(CustomerDAO.class);

    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY name";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
            logger.info("Retrieved " + customers.size() + " customers");
        } catch (SQLException e) {
            logger.error("Error retrieving customers", e);
        }
        return customers;
    }

    public Customer getCustomerById(int id) {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToCustomer(rs);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving customer by id", e);
        }
        return null;
    }

    public List<Customer> searchCustomers(String keyword) {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE name LIKE ? OR phone LIKE ? ORDER BY name";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String searchTerm = "%" + keyword + "%";
            ps.setString(1, searchTerm);
            ps.setString(2, searchTerm);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
            logger.info("Search found " + customers.size() + " customers for keyword: " + keyword);
        } catch (SQLException e) {
            logger.error("Error searching customers", e);
        }
        return customers;
    }

    public boolean createCustomer(Customer customer) {
        String sql = "INSERT INTO customers (name, phone, created_at) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getPhone());
            ps.setLong(3, System.currentTimeMillis());
            int affectedRows = ps.executeUpdate();
            logger.info("Customer created: " + customer.getName());
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error creating customer", e);
        }
        return false;
    }

    public boolean updateCustomer(Customer customer) {
        String sql = "UPDATE customers SET name = ?, phone = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getPhone());
            ps.setInt(3, customer.getId());
            int affectedRows = ps.executeUpdate();
            logger.info("Customer updated: " + customer.getName());
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error updating customer", e);
        }
        return false;
    }

    public boolean deleteCustomer(int id) {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affectedRows = ps.executeUpdate();
            logger.info("Customer deleted with id: " + id);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error deleting customer", e);
        }
        return false;
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("phone")
        );
        customer.setCreatedAt(rs.getLong("created_at"));
        return customer;
    }
}


package org.example.dao;

import org.example.database.DatabaseUtil;
import org.example.model.Order;
import org.example.model.OrderDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    private static final Logger logger = LoggerFactory.getLogger(OrderDAO.class);

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, c.name as customer_name FROM orders o " +
                     "LEFT JOIN customers c ON o.customer_id = c.id ORDER BY o.order_date DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
            logger.info("Retrieved " + orders.size() + " orders");
        } catch (SQLException e) {
            logger.error("Error retrieving orders", e);
        }
        return orders;
    }

    public Order getOrderById(int id) {
        String sql = "SELECT o.*, c.name as customer_name FROM orders o " +
                     "LEFT JOIN customers c ON o.customer_id = c.id WHERE o.id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToOrder(rs);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving order by id", e);
        }
        return null;
    }

    public List<Order> getOrdersByCustomer(int customerId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, c.name as customer_name FROM orders o " +
                     "LEFT JOIN customers c ON o.customer_id = c.id " +
                     "WHERE o.customer_id = ? ORDER BY o.order_date DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving orders by customer", e);
        }
        return orders;
    }

    public List<Order> getOrdersByStatus(String status) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, c.name as customer_name FROM orders o " +
                     "LEFT JOIN customers c ON o.customer_id = c.id " +
                     "WHERE o.status = ? ORDER BY o.order_date DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving orders by status", e);
        }
        return orders;
    }

    public int createOrder(Order order) {
        String sql = "INSERT INTO orders (customer_id, total_amount, status, order_date, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order.getCustomerId());
            ps.setDouble(2, order.getTotalAmount());
            ps.setString(3, order.getStatus());
            ps.setLong(4, order.getOrderDate());
            ps.setLong(5, order.getUpdatedAt());
            ps.executeUpdate();
            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                int orderId = generatedKeys.getInt(1);
                logger.info("Order created with id: " + orderId);
                return orderId;
            }
        } catch (SQLException e) {
            logger.error("Error creating order", e);
        }
        return -1;
    }

    public boolean updateOrder(Order order) {
        String sql = "UPDATE orders SET customer_id = ?, total_amount = ?, status = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, order.getCustomerId());
            ps.setDouble(2, order.getTotalAmount());
            ps.setString(3, order.getStatus());
            ps.setLong(4, System.currentTimeMillis());
            ps.setInt(5, order.getId());
            int affectedRows = ps.executeUpdate();
            logger.info("Order updated: " + order.getId());
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error updating order", e);
        }
        return false;
    }

    public boolean deleteOrder(int id) {
        String sql = "DELETE FROM orders WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affectedRows = ps.executeUpdate();
            logger.info("Order deleted with id: " + id);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error deleting order", e);
        }
        return false;
    }

    public List<OrderDetail> getOrderDetails(int orderId) {
        List<OrderDetail> details = new ArrayList<>();
        String sql = "SELECT od.*, p.name as product_name FROM order_details od " +
                     "LEFT JOIN products p ON od.product_id = p.id WHERE od.order_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                OrderDetail detail = new OrderDetail(
                    rs.getInt("id"),
                    rs.getInt("order_id"),
                    rs.getInt("product_id"),
                    rs.getInt("quantity"),
                    rs.getDouble("unit_price")
                );
                detail.setProductName(rs.getString("product_name"));
                details.add(detail);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving order details", e);
        }
        return details;
    }

    public boolean createOrderDetail(OrderDetail detail) {
        String sql = "INSERT INTO order_details (order_id, product_id, quantity, unit_price, total_price) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setInt(1, detail.getOrderId());
                ps.setInt(2, detail.getProductId());
                ps.setInt(3, detail.getQuantity());
                ps.setDouble(4, detail.getUnitPrice());
                ps.setDouble(5, detail.getTotalPrice());
                int affectedRows = ps.executeUpdate();
                
                conn.commit();
                return affectedRows > 0;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Error creating order detail", e);
        }
        return false;
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order(
            rs.getInt("id"),
            rs.getInt("customer_id"),
            rs.getDouble("total_amount"),
            rs.getString("status")
        );
        order.setCustomerName(rs.getString("customer_name"));
        order.setOrderDate(rs.getLong("order_date"));
        order.setUpdatedAt(rs.getLong("updated_at"));
        return order;
    }
}


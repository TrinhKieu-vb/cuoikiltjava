package org.example.server;

import org.example.dao.*;
import org.example.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class ServerService {
    private static final Logger logger = LoggerFactory.getLogger(ServerService.class);
    private static final UserDAO userDAO = new UserDAO();
    private static final ProductDAO productDAO = new ProductDAO();
    private static final CustomerDAO customerDAO = new CustomerDAO();
    private static final OrderDAO orderDAO = new OrderDAO();
    private static final CategoryDAO categoryDAO = new CategoryDAO();
    private static final Map<String, User> sessions = Collections.synchronizedMap(new HashMap<>());

    /**
     * Authenticate user
     */
    public static User authenticateUser(String username, String password) {
        User user = userDAO.authenticate(username, password);
        if (user != null) {
            logger.info("User authenticated: " + username);
        }
        return user;
    }

    /**
     * Create session
     */
    public static String createSession(User user) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, user);
        logger.info("Session created for user: " + user.getUsername());
        return sessionId;
    }

    /**
     * Validate session
     */
    public static User getSessionUser(String sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * Invalidate session
     */
    public static void invalidateSession(String sessionId) {
        sessions.remove(sessionId);
        logger.info("Session invalidated: " + sessionId);
    }

    // ========== PRODUCT OPERATIONS ==========

    public static List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }

    public static Product getProductById(int id) {
        return productDAO.getProductById(id);
    }

    public static List<Product> searchProducts(String keyword) {
        return productDAO.searchProducts(keyword);
    }

    public static List<Product> getProductsByCategory(int categoryId) {
        return productDAO.getProductsByCategory(categoryId);
    }

    public static boolean createProduct(Product product) {
        return productDAO.createProduct(product);
    }

    public static boolean updateProduct(Product product) {
        return productDAO.updateProduct(product);
    }

    public static boolean deleteProduct(int id) {
        return productDAO.deleteProduct(id);
    }

    // ========== CUSTOMER OPERATIONS ==========

    public static List<Customer> getAllCustomers() {
        return customerDAO.getAllCustomers();
    }

    public static Customer getCustomerById(int id) {
        return customerDAO.getCustomerById(id);
    }

    public static List<Customer> searchCustomers(String keyword) {
        return customerDAO.searchCustomers(keyword);
    }

    public static boolean createCustomer(Customer customer) {
        return customerDAO.createCustomer(customer);
    }

    public static boolean updateCustomer(Customer customer) {
        return customerDAO.updateCustomer(customer);
    }

    public static boolean deleteCustomer(int id) {
        return customerDAO.deleteCustomer(id);
    }

    // ========== ORDER OPERATIONS ==========

    public static List<Order> getAllOrders() {
        return orderDAO.getAllOrders();
    }

    public static Order getOrderById(int id) {
        Order order = orderDAO.getOrderById(id);
        if (order != null) {
            // Load order details
            List<OrderDetail> details = orderDAO.getOrderDetails(id);
            // Could store details in Order object if needed
        }
        return order;
    }

    public static List<Order> getOrdersByCustomer(int customerId) {
        return orderDAO.getOrdersByCustomer(customerId);
    }

    public static List<Order> getOrdersByStatus(String status) {
        return orderDAO.getOrdersByStatus(status);
    }

    public static int createOrder(Order order) {
        return orderDAO.createOrder(order);
    }

    public static boolean updateOrder(Order order) {
        return orderDAO.updateOrder(order);
    }

    public static boolean deleteOrder(int id) {
        return orderDAO.deleteOrder(id);
    }

    public static List<OrderDetail> getOrderDetails(int orderId) {
        return orderDAO.getOrderDetails(orderId);
    }

    public static boolean createOrderDetail(OrderDetail detail) {
        return orderDAO.createOrderDetail(detail);
    }

    // ========== CATEGORY OPERATIONS ==========

    public static List<Category> getAllCategories() {
        return categoryDAO.getAllCategories();
    }

    public static Category getCategoryById(int id) {
        return categoryDAO.getCategoryById(id);
    }

    public static boolean createCategory(Category category) {
        return categoryDAO.createCategory(category);
    }

    public static boolean updateCategory(Category category) {
        return categoryDAO.updateCategory(category);
    }

    public static boolean deleteCategory(int id) {
        return categoryDAO.deleteCategory(id);
    }

    // ========== USER OPERATIONS (for admin) ==========

    public static List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public static User getUserById(int id) {
        return userDAO.getUserById(id);
    }

    public static boolean createUser(User user) {
        return userDAO.createUser(user);
    }

    public static boolean updateUser(User user) {
        return userDAO.updateUser(user);
    }

    public static boolean deleteUser(int id) {
        return userDAO.deleteUser(id);
    }

    /**
     * Get statistics for dashboard
     */
    public static Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            stats.put("totalProducts", getAllProducts().size());
            stats.put("totalCustomers", getAllCustomers().size());
            stats.put("totalOrders", getAllOrders().size());
            stats.put("totalCategories", getAllCategories().size());

            // Calculate total revenue
            double totalRevenue = getAllOrders().stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();
            stats.put("totalRevenue", totalRevenue);

            logger.info("Dashboard statistics retrieved");
        } catch (Exception e) {
            logger.error("Error retrieving dashboard statistics", e);
        }
        return stats;
    }

    public static boolean changePassword(int userId, String oldPassword, String newPassword) {
        return userDAO.changePassword(userId, oldPassword, newPassword);
    }
}


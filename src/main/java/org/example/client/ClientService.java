package org.example.client;

import org.example.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientService {
    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);
    private NetworkClient networkClient = NetworkClient.getInstance();

    /**
     * Login user
     */
    public User login(String username, String password) {
        try {
            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", username);
            credentials.put("password", password);

            Request request = new Request("AUTH", "LOGIN", credentials);
            Response response = networkClient.sendRequest(request);

            if (response.isSuccess() && response.getData() instanceof User) {
                User user = (User) response.getData();
                SessionManager.getInstance().setCurrentUser(user);
                logger.info("User logged in: " + username);
                return user;
            } else {
                logger.warn("Login failed: " + response.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error during login", e);
        }
        return null;
    }

    // ========== PRODUCT SERVICE ==========

    public List<Product> getAllProducts() {
        try {
            Request request = new Request("PRODUCT", "GET_ALL", null);
            Response response = networkClient.sendRequest(request);
            if (response.isSuccess()) {
                @SuppressWarnings("unchecked")
                List<Product> products = (List<Product>) response.getData();
                return products;
            }
        } catch (Exception e) {
            logger.error("Error getting products", e);
        }
        return null;
    }

    public Product getProductById(int id) {
        try {
            Request request = new Request("PRODUCT", "GET_BY_ID", id);
            Response response = networkClient.sendRequest(request);
            if (response.isSuccess()) {
                return (Product) response.getData();
            }
        } catch (Exception e) {
            logger.error("Error getting product", e);
        }
        return null;
    }

    public List<Product> searchProducts(String keyword) {
        try {
            Request request = new Request("PRODUCT", "SEARCH", keyword);
            Response response = networkClient.sendRequest(request);
            if (response.isSuccess()) {
                @SuppressWarnings("unchecked")
                List<Product> products = (List<Product>) response.getData();
                return products;
            }
        } catch (Exception e) {
            logger.error("Error searching products", e);
        }
        return null;
    }

    public boolean createProduct(Product product) {
        try {
            Request request = new Request("PRODUCT", "CREATE", product);
            Response response = networkClient.sendRequest(request);
            return response.isSuccess();
        } catch (Exception e) {
            logger.error("Error creating product", e);
        }
        return false;
    }

    public boolean updateProduct(Product product) {
        try {
            Request request = new Request("PRODUCT", "UPDATE", product);
            Response response = networkClient.sendRequest(request);
            return response.isSuccess();
        } catch (Exception e) {
            logger.error("Error updating product", e);
        }
        return false;
    }

    public boolean deleteProduct(int id) {
        try {
            Request request = new Request("PRODUCT", "DELETE", id);
            Response response = networkClient.sendRequest(request);
            return response.isSuccess();
        } catch (Exception e) {
            logger.error("Error deleting product", e);
        }
        return false;
    }

    // ========== CUSTOMER SERVICE ==========

    public List<Customer> getAllCustomers() {
        try {
            Request request = new Request("CUSTOMER", "GET_ALL", null);
            Response response = networkClient.sendRequest(request);
            if (response.isSuccess()) {
                @SuppressWarnings("unchecked")
                List<Customer> customers = (List<Customer>) response.getData();
                return customers;
            }
        } catch (Exception e) {
            logger.error("Error getting customers", e);
        }
        return null;
    }

    public Customer getCustomerById(int id) {
        try {
            Request request = new Request("CUSTOMER", "GET_BY_ID", id);
            Response response = networkClient.sendRequest(request);
            if (response.isSuccess()) {
                return (Customer) response.getData();
            }
        } catch (Exception e) {
            logger.error("Error getting customer", e);
        }
        return null;
    }

    public List<Customer> searchCustomers(String keyword) {
        try {
            Request request = new Request("CUSTOMER", "SEARCH", keyword);
            Response response = networkClient.sendRequest(request);
            if (response.isSuccess()) {
                @SuppressWarnings("unchecked")
                List<Customer> customers = (List<Customer>) response.getData();
                return customers;
            }
        } catch (Exception e) {
            logger.error("Error searching customers", e);
        }
        return null;
    }

    public boolean createCustomer(Customer customer) {
        try {
            Request request = new Request("CUSTOMER", "CREATE", customer);
            Response response = networkClient.sendRequest(request);
            return response.isSuccess();
        } catch (Exception e) {
            logger.error("Error creating customer", e);
        }
        return false;
    }

    public boolean updateCustomer(Customer customer) {
        try {
            Request request = new Request("CUSTOMER", "UPDATE", customer);
            Response response = networkClient.sendRequest(request);
            return response.isSuccess();
        } catch (Exception e) {
            logger.error("Error updating customer", e);
        }
        return false;
    }

    public boolean deleteCustomer(int id) {
        try {
            Request request = new Request("CUSTOMER", "DELETE", id);
            Response response = networkClient.sendRequest(request);
            return response.isSuccess();
        } catch (Exception e) {
            logger.error("Error deleting customer", e);
        }
        return false;
    }

    // ========== ORDER SERVICE ==========

    public List<Order> getAllOrders() {
        try {
            Request request = new Request("ORDER", "GET_ALL", null);
            Response response = networkClient.sendRequest(request);
            if (response.isSuccess()) {
                @SuppressWarnings("unchecked")
                List<Order> orders = (List<Order>) response.getData();
                return orders;
            }
        } catch (Exception e) {
            logger.error("Error getting orders", e);
        }
        return null;
    }

    public Order getOrderById(int id) {
        try {
            Request request = new Request("ORDER", "GET_BY_ID", id);
            Response response = networkClient.sendRequest(request);
            if (response.isSuccess()) {
                return (Order) response.getData();
            }
        } catch (Exception e) {
            logger.error("Error getting order", e);
        }
        return null;
    }

    public int createOrder(Order order) {
        try {
            Request request = new Request("ORDER", "CREATE", order);
            Response response = networkClient.sendRequest(request);
            if (response.isSuccess() && response.getData() instanceof Integer) {
                return (Integer) response.getData();
            }
        } catch (Exception e) {
            logger.error("Error creating order", e);
        }
        return -1;
    }

    public boolean createOrderDetail(OrderDetail detail) {
        try {
            Request request = new Request("ORDER", "CREATE_DETAIL", detail);
            Response response = networkClient.sendRequest(request);
            return response.isSuccess();
        } catch (Exception e) {
            logger.error("Error creating order detail", e);
        }
        return false;
    }

    public List<OrderDetail> getOrderDetails(int orderId) {
        try {
            Request request = new Request("ORDER", "GET_DETAILS", orderId);
            Response response = networkClient.sendRequest(request);
            if (response.isSuccess()) {
                @SuppressWarnings("unchecked")
                List<OrderDetail> details = (List<OrderDetail>) response.getData();
                return details;
            }
        } catch (Exception e) {
            logger.error("Error getting order details", e);
        }
        return null;
    }

    public boolean updateOrder(Order order) {
        try {
            Request request = new Request("ORDER", "UPDATE", order);
            Response response = networkClient.sendRequest(request);
            return response.isSuccess();
        } catch (Exception e) {
            logger.error("Error updating order", e);
        }
        return false;
    }

    public boolean deleteOrder(int id) {
        try {
            Request request = new Request("ORDER", "DELETE", id);
            Response response = networkClient.sendRequest(request);
            return response.isSuccess();
        } catch (Exception e) {
            logger.error("Error deleting order", e);
        }
        return false;
    }

    // ========== CATEGORY SERVICE ==========

    public List<Category> getAllCategories() {
        try {
            Request request = new Request("CATEGORY", "GET_ALL", null);
            Response response = networkClient.sendRequest(request);
            if (response.isSuccess()) {
                @SuppressWarnings("unchecked")
                List<Category> categories = (List<Category>) response.getData();
                return categories;
            }
        } catch (Exception e) {
            logger.error("Error getting categories", e);
        }
        return null;
    }

    public Category getCategoryById(int id) {
        try {
            Request request = new Request("CATEGORY", "GET_BY_ID", id);
            Response response = networkClient.sendRequest(request);
            if (response.isSuccess()) {
                return (Category) response.getData();
            }
        } catch (Exception e) {
            logger.error("Error getting category", e);
        }
        return null;
    }

    public boolean createCategory(Category category) {
        try {
            Request request = new Request("CATEGORY", "CREATE", category);
            Response response = networkClient.sendRequest(request);
            return response.isSuccess();
        } catch (Exception e) {
            logger.error("Error creating category", e);
        }
        return false;
    }

    public boolean updateCategory(Category category) {
        try {
            Request request = new Request("CATEGORY", "UPDATE", category);
            Response response = networkClient.sendRequest(request);
            return response.isSuccess();
        } catch (Exception e) {
            logger.error("Error updating category", e);
        }
        return false;
    }

    public boolean deleteCategory(int id) {
        try {
            Request request = new Request("CATEGORY", "DELETE", id);
            Response response = networkClient.sendRequest(request);
            return response.isSuccess();
        } catch (Exception e) {
            logger.error("Error deleting category", e);
        }
        return false;
    }

    // ========== USER SERVICE ==========

    public List<User> getAllUsers() {
        try {
            Request request = new Request("USER", "GET_ALL", null);
            Response response = networkClient.sendRequest(request);
            if (response.isSuccess()) {
                @SuppressWarnings("unchecked")
                List<User> users = (List<User>) response.getData();
                return users;
            }
        } catch (Exception e) {
            logger.error("Error getting users", e);
        }
        return null;
    }

    public boolean createUser(User user) {
        try {
            Request request = new Request("USER", "CREATE", user);
            Response response = networkClient.sendRequest(request);
            return response.isSuccess();
        } catch (Exception e) {
            logger.error("Error creating user", e);
        }
        return false;
    }

    public boolean updateUser(User user) {
        try {
            Request request = new Request("USER", "UPDATE", user);
            Response response = networkClient.sendRequest(request);
            return response.isSuccess();
        } catch (Exception e) {
            logger.error("Error updating user", e);
        }
        return false;
    }

    public boolean deleteUser(int id) {
        try {
            Request request = new Request("USER", "DELETE", id);
            Response response = networkClient.sendRequest(request);
            return response.isSuccess();
        } catch (Exception e) {
            logger.error("Error deleting user", e);
        }
        return false;
    }

    public boolean changePassword(String oldPassword, String newPassword) {
        try {
            Map<String, String> pwdData = new HashMap<>();
            pwdData.put("oldPassword", oldPassword);
            pwdData.put("newPassword", newPassword);
            Request request = new Request("USER", "CHANGE_PASSWORD", pwdData);
            Response response = networkClient.sendRequest(request);
            return response.isSuccess();
        } catch (Exception e) {
            logger.error("Error changing password", e);
        }
        return false;
    }

    public boolean updateProfile(User user) {
        try {
            Request request = new Request("USER", "UPDATE_PROFILE", user);
            Response response = networkClient.sendRequest(request);
            if (response.isSuccess() && response.getData() instanceof User) {
                User updatedUser = (User) response.getData();
                SessionManager.getInstance().setCurrentUser(updatedUser);
                return true;
            }
        } catch (Exception e) {
            logger.error("Error updating profile", e);
        }
        return false;
    }

    // ========== DASHBOARD SERVICE ==========

    public Map<String, Object> getDashboardStats() {
        try {
            Request request = new Request("DASHBOARD", "GET_STATS", null);
            Response response = networkClient.sendRequest(request);
            if (response.isSuccess()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> stats = (Map<String, Object>) response.getData();
                return stats;
            }
        } catch (Exception e) {
            logger.error("Error getting dashboard stats", e);
        }
        return null;
    }
}


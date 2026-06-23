package org.example.server;

import org.example.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private User currentUser;
    private String sessionId;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // Initialize streams (order matters: output first)
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());

            logger.info("ClientHandler started for " + socket.getInetAddress());

            // Process client requests
            while (socket.isConnected()) {
                try {
                    Object receivedObject = input.readObject();
                    if (receivedObject instanceof Request) {
                        Request request = (Request) receivedObject;
                        Response response = handleRequest(request);
                        output.writeObject(response);
                        output.flush();
                    }
                } catch (EOFException e) {
                    logger.info("Client disconnected: " + socket.getInetAddress());
                    break;
                }
            }
        } catch (IOException e) {
            logger.error("IO Error handling client", e);
        } catch (ClassNotFoundException e) {
            logger.error("Class not found error", e);
        } finally {
            closeConnections();
        }
    }

    /**
     * Handle incoming request
     */
    private Response handleRequest(Request request) {
        try {
            String action = request.getAction();
            String method = request.getMethod();
            Object data = request.getData();

            logger.info("Processing request: action=" + action + ", method=" + method);

            switch (action) {
                case "AUTH":
                    return handleAuthRequest(method, data);
                case "PRODUCT":
                    return handleProductRequest(method, data);
                case "CUSTOMER":
                    return handleCustomerRequest(method, data);
                case "ORDER":
                    return handleOrderRequest(method, data);
                case "CATEGORY":
                    return handleCategoryRequest(method, data);
                case "USER":
                    return handleUserRequest(method, data);
                case "DASHBOARD":
                    return handleDashboardRequest(method, data);
                default:
                    return new Response(false, "400", "Unknown action: " + action, null);
            }
        } catch (Exception e) {
            logger.error("Error processing request", e);
            return new Response(false, "500", "Server error: " + e.getMessage(), null);
        }
    }

    /**
     * Handle authentication requests
     */
    private Response handleAuthRequest(String method, Object data) {
        if ("LOGIN".equals(method)) {
            if (data instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, String> credentials = (Map<String, String>) data;
                String username = credentials.get("username");
                String password = credentials.get("password");

                User user = ServerService.authenticateUser(username, password);
                if (user != null) {
                    currentUser = user;
                    sessionId = ServerService.createSession(user);
                    logger.info("User logged in: " + username);
                    return new Response(true, "200", "Login successful", user);
                } else {
                    logger.warn("Failed login attempt for: " + username);
                    return new Response(false, "401", "Invalid username or password", null);
                }
            }
        }
        return new Response(false, "400", "Invalid auth request", null);
    }

    /**
     * Handle product requests
     */
    private Response handleProductRequest(String method, Object data) {
        if (currentUser == null) {
            return new Response(false, "401", "Not authenticated", null);
        }

        switch (method) {
            case "GET_ALL":
                return new Response(true, "200", "Products retrieved", ServerService.getAllProducts());
            case "GET_BY_ID":
                if (data instanceof Integer) {
                    return new Response(true, "200", "Product retrieved",
                        ServerService.getProductById((Integer) data));
                }
                break;
            case "SEARCH":
                if (data instanceof String) {
                    return new Response(true, "200", "Search completed",
                        ServerService.searchProducts((String) data));
                }
                break;
            case "CREATE":
                if (currentUser.getRole().equals("ADMIN")) {
                    if (data instanceof Product) {
                        boolean success = ServerService.createProduct((Product) data);
                        return new Response(success, success ? "200" : "400",
                            success ? "Product created" : "Failed to create product", null);
                    }
                } else {
                    return new Response(false, "403", "Unauthorized: Admin only", null);
                }
                break;
            case "UPDATE":
                if (currentUser.getRole().equals("ADMIN")) {
                    if (data instanceof Product) {
                        boolean success = ServerService.updateProduct((Product) data);
                        return new Response(success, success ? "200" : "400",
                            success ? "Product updated" : "Failed to update product", null);
                    }
                } else {
                    return new Response(false, "403", "Unauthorized: Admin only", null);
                }
                break;
            case "DELETE":
                if (currentUser.getRole().equals("ADMIN")) {
                    if (data instanceof Integer) {
                        boolean success = ServerService.deleteProduct((Integer) data);
                        return new Response(success, success ? "200" : "400",
                            success ? "Product deleted" : "Failed to delete product", null);
                    }
                } else {
                    return new Response(false, "403", "Unauthorized: Admin only", null);
                }
                break;
        }
        return new Response(false, "400", "Invalid product request", null);
    }

    /**
     * Handle customer requests
     */
    private Response handleCustomerRequest(String method, Object data) {
        if (currentUser == null) {
            return new Response(false, "401", "Not authenticated", null);
        }

        switch (method) {
            case "GET_ALL":
                return new Response(true, "200", "Customers retrieved", ServerService.getAllCustomers());
            case "GET_BY_ID":
                if (data instanceof Integer) {
                    return new Response(true, "200", "Customer retrieved",
                        ServerService.getCustomerById((Integer) data));
                }
                break;
            case "SEARCH":
                if (data instanceof String) {
                    return new Response(true, "200", "Search completed",
                        ServerService.searchCustomers((String) data));
                }
                break;
            case "CREATE":
                if (data instanceof Customer) {
                    boolean success = ServerService.createCustomer((Customer) data);
                    return new Response(success, success ? "200" : "400",
                        success ? "Customer created" : "Failed to create customer", null);
                }
                break;
            case "UPDATE":
                if (data instanceof Customer) {
                    boolean success = ServerService.updateCustomer((Customer) data);
                    return new Response(success, success ? "200" : "400",
                        success ? "Customer updated" : "Failed to update customer", null);
                }
                break;
            case "DELETE":
                if (currentUser.getRole().equals("ADMIN")) {
                    if (data instanceof Integer) {
                        boolean success = ServerService.deleteCustomer((Integer) data);
                        return new Response(success, success ? "200" : "400",
                            success ? "Customer deleted" : "Failed to delete customer", null);
                    }
                } else {
                    return new Response(false, "403", "Unauthorized: Admin only", null);
                }
                break;
        }
        return new Response(false, "400", "Invalid customer request", null);
    }

    /**
     * Handle order requests
     */
    private Response handleOrderRequest(String method, Object data) {
        if (currentUser == null) {
            return new Response(false, "401", "Not authenticated", null);
        }

        switch (method) {
            case "GET_ALL":
                return new Response(true, "200", "Orders retrieved", ServerService.getAllOrders());
            case "GET_BY_ID":
                if (data instanceof Integer) {
                    return new Response(true, "200", "Order retrieved",
                        ServerService.getOrderById((Integer) data));
                }
                break;
            case "GET_DETAILS":
                if (data instanceof Integer) {
                    return new Response(true, "200", "Order details retrieved",
                        ServerService.getOrderDetails((Integer) data));
                }
                break;
            case "CREATE":
                if (data instanceof Order) {
                    int orderId = ServerService.createOrder((Order) data);
                    boolean success = orderId > 0;
                    return new Response(success, success ? "200" : "400",
                        success ? "Order created" : "Failed to create order", orderId);
                }
                break;
            case "CREATE_DETAIL":
                if (data instanceof OrderDetail) {
                    boolean success = ServerService.createOrderDetail((OrderDetail) data);
                    return new Response(success, success ? "200" : "400",
                        success ? "Order detail created" : "Failed to create order detail", null);
                }
                break;
            case "UPDATE":
                if (data instanceof Order) {
                    boolean success = ServerService.updateOrder((Order) data);
                    return new Response(success, success ? "200" : "400",
                        success ? "Order updated" : "Failed to update order", null);
                }
                break;
            case "DELETE":
                if (currentUser.getRole().equals("ADMIN")) {
                    if (data instanceof Integer) {
                        boolean success = ServerService.deleteOrder((Integer) data);
                        return new Response(success, success ? "200" : "400",
                            success ? "Order deleted" : "Failed to delete order", null);
                    }
                } else {
                    return new Response(false, "403", "Unauthorized: Admin only", null);
                }
                break;
        }
        return new Response(false, "400", "Invalid order request", null);
    }

    /**
     * Handle user requests (Admin only)
     */
    private Response handleUserRequest(String method, Object data) {
        if (currentUser == null) {
            return new Response(false, "401", "Not authenticated", null);
        }

        if ("CHANGE_PASSWORD".equals(method)) {
            if (data instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, String> pwdData = (Map<String, String>) data;
                String oldPwd = pwdData.get("oldPassword");
                String newPwd = pwdData.get("newPassword");
                boolean success = ServerService.changePassword(currentUser.getId(), oldPwd, newPwd);
                return new Response(success, success ? "200" : "400",
                    success ? "Đổi mật khẩu thành công!" : "Mật khẩu cũ không chính xác", null);
            }
        }

        if ("UPDATE_PROFILE".equals(method)) {
            if (data instanceof User) {
                User u = (User) data;
                if (u.getId() == currentUser.getId()) {
                    currentUser.setEmail(u.getEmail());
                    boolean success = ServerService.updateUser(currentUser);
                    return new Response(success, success ? "200" : "400",
                        success ? "Cập nhật thông tin thành công!" : "Không thể cập nhật thông tin", currentUser);
                }
            }
        }

        if (!"ADMIN".equals(currentUser.getRole())) {
            return new Response(false, "403", "Unauthorized: Admin only", null);
        }

        switch (method) {
            case "GET_ALL":
                return new Response(true, "200", "Users retrieved", ServerService.getAllUsers());
            case "GET_BY_ID":
                if (data instanceof Integer) {
                    return new Response(true, "200", "User retrieved", ServerService.getUserById((Integer) data));
                }
                break;
            case "CREATE":
                if (data instanceof User) {
                    boolean success = ServerService.createUser((User) data);
                    return new Response(success, success ? "200" : "400",
                        success ? "User created" : "Failed to create user", null);
                }
                break;
            case "UPDATE":
                if (data instanceof User) {
                    boolean success = ServerService.updateUser((User) data);
                    return new Response(success, success ? "200" : "400",
                        success ? "User updated" : "Failed to update user", null);
                }
                break;
            case "DELETE":
                if (data instanceof Integer) {
                    boolean success = ServerService.deleteUser((Integer) data);
                    return new Response(success, success ? "200" : "400",
                        success ? "User deleted" : "Failed to delete user", null);
                }
                break;
        }
        return new Response(false, "400", "Invalid user request", null);
    }

    /**
     * Handle dashboard requests
     */
    private Response handleDashboardRequest(String method, Object data) {
        if (currentUser == null) {
            return new Response(false, "401", "Not authenticated", null);
        }

        if ("GET_STATS".equals(method)) {
            return new Response(true, "200", "Stats retrieved", ServerService.getDashboardStats());
        }
        return new Response(false, "400", "Invalid dashboard request", null);
    }

    /**
     * Handle category requests
     */
    private Response handleCategoryRequest(String method, Object data) {
        if (currentUser == null) {
            return new Response(false, "401", "Not authenticated", null);
        }

        switch (method) {
            case "GET_ALL":
                return new Response(true, "200", "Categories retrieved", ServerService.getAllCategories());
            case "GET_BY_ID":
                if (data instanceof Integer) {
                    return new Response(true, "200", "Category retrieved",
                        ServerService.getCategoryById((Integer) data));
                }
                break;
            case "CREATE":
                if (currentUser.getRole().equals("ADMIN")) {
                    if (data instanceof Category) {
                        boolean success = ServerService.createCategory((Category) data);
                        return new Response(success, success ? "200" : "400",
                            success ? "Category created" : "Failed to create category", null);
                    }
                } else {
                    return new Response(false, "403", "Unauthorized: Admin only", null);
                }
                break;
            case "UPDATE":
                if (currentUser.getRole().equals("ADMIN")) {
                    if (data instanceof Category) {
                        boolean success = ServerService.updateCategory((Category) data);
                        return new Response(success, success ? "200" : "400",
                            success ? "Category updated" : "Failed to update category", null);
                    }
                } else {
                    return new Response(false, "403", "Unauthorized: Admin only", null);
                }
                break;
            case "DELETE":
                if (currentUser.getRole().equals("ADMIN")) {
                    if (data instanceof Integer) {
                        boolean success = ServerService.deleteCategory((Integer) data);
                        return new Response(success, success ? "200" : "400",
                            success ? "Category deleted" : "Failed to delete category", null);
                    }
                } else {
                    return new Response(false, "403", "Unauthorized: Admin only", null);
                }
                break;
        }
        return new Response(false, "400", "Invalid category request", null);
    }

    /**
     * Close connections
     */
    private void closeConnections() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
            logger.info("Client connection closed");
        } catch (IOException e) {
            logger.error("Error closing connections", e);
        }
    }
}


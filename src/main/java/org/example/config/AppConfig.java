package org.example.config;

public class AppConfig {
    // Server Configuration
    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 5555;
    public static final int THREAD_POOL_SIZE = 20;

    // Database Configuration
    public static final String DB_URL = "jdbc:mysql://localhost:3306/drink_dessert_management";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "trinh226";

    // Client Configuration
    public static final int CONNECTION_TIMEOUT = 5000;

    // Session Configuration
    public static final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 10;

    // Role Configuration
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";

    // File Configuration
    public static final String EXPORT_PATH = "exports/";
}


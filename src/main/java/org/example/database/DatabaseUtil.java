package org.example.database;

import org.example.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;

public class DatabaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);

    /**
     * Get database connection
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                AppConfig.DB_URL,
                AppConfig.DB_USER,
                AppConfig.DB_PASSWORD
            );
            logger.info("Database connection established successfully");
            return conn;
        } catch (ClassNotFoundException e) {
            logger.error("MySQL Driver not found", e);
            throw new SQLException("MySQL Driver not found", e);
        } catch (SQLException e) {
            logger.error("Failed to connect to database", e);
            throw e;
        }
    }

    /**
     * Close database connection
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                logger.debug("Database connection closed");
            } catch (SQLException e) {
                logger.error("Error closing connection", e);
            }
        }
    }

    /**
     * Close resources
     */
    public static void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            logger.error("Error closing resources", e);
        }
    }

    /**
     * Test database connection
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }
}


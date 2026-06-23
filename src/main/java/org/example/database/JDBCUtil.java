package org.example.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCUtil {
    public static Connection getConnection() {
        Connection c = null;
        try {
            // Các thông số kết nối database
            String url = "jdbc:mysql://localhost:3306/drink_dessert_management";
            String username = "root";
            String password = "trinh226"; 

            System.out.println("Đang cố gắng kết nối tới: " + url);
            // JDBC 4.0+ sẽ tự động tìm nạp Driver, không cần gọi registerDriver thủ công
            c = DriverManager.getConnection(url, username, password);
            System.out.println("Kết nối thành công tới database!");
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối chi tiết: ");
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
        return c;
    }

    public static void closeConnection(Connection c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

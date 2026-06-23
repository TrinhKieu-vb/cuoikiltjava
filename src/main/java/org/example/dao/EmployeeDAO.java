package org.example.dao;

import org.example.model.Employee;
import org.example.database.JDBCUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {
    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT id, full_name FROM users";
        try (Connection conn = JDBCUtil.getConnection()) {
            if (conn == null) {
                System.err.println("Không thể kết nối tới database!");
                return employees;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    employees.add(new Employee(
                        rs.getInt("id"),
                        rs.getString("full_name")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy dữ liệu nhân viên: " + e.getMessage());
            e.printStackTrace();
        }
        return employees;
    }

    public void addEmployee(Employee employee) {
        String sql = "INSERT INTO users (full_name) VALUES (?)";
        try (Connection conn = JDBCUtil.getConnection()) {
            if (conn == null) {
                System.err.println("Không thể kết nối tới database!");
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, employee.getFullName());
                ps.executeUpdate();
                System.out.println("Đã thêm nhân viên thành công!");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm nhân viên: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
package org.example.dao;

import org.example.database.DatabaseUtil;
import org.example.security.PasswordUtil;
import org.example.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    /**
     * Get user by username and password
     */
    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hashedPassword = rs.getString("password");

                System.out.println("===== LOGIN DEBUG =====");
                System.out.println("Username: " + username);
                System.out.println("Password nhập vào: " + password);
                System.out.println("Hash trong DB: " + hashedPassword);

                boolean ok = PasswordUtil.verifyPassword(password, hashedPassword);

                System.out.println("Verify result: " + ok);
                System.out.println("=======================");

                if (ok) {
                    return mapResultSetToUser(rs);
                }
            }
            logger.warn("Authentication failed for user: " + username);
        } catch (SQLException e) {
            logger.error("Error authenticating user", e);
        }
        return null;
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            logger.info("Retrieved " + users.size() + " users");
        } catch (SQLException e) {
            logger.error("Error retrieving users", e);
        }
        return users;
    }

    /**
     * Get user by ID
     */
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving user by id", e);
        }
        return null;
    }

    /**
     * Create new user
     */
    public boolean createUser(User user) {
        String sql = "INSERT INTO users (username, password, email, role, active) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, PasswordUtil.hashPassword(user.getPassword()));
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getRole());
            ps.setBoolean(5, user.isActive());
            int affectedRows = ps.executeUpdate();
            logger.info("User created: " + user.getUsername());
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error creating user", e);
        }
        return false;
    }

    /**
     * Update user
     */
    public boolean updateUser(User user) {
        boolean updatePassword = user.getPassword() != null && !user.getPassword().isEmpty();
        String sql = updatePassword
            ? "UPDATE users SET username = ?, email = ?, role = ?, active = ?, password = ? WHERE id = ?"
            : "UPDATE users SET username = ?, email = ?, role = ?, active = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getRole());
            ps.setBoolean(4, user.isActive());
            if (updatePassword) {
                ps.setString(5, PasswordUtil.hashPassword(user.getPassword()));
                ps.setInt(6, user.getId());
            } else {
                ps.setInt(5, user.getId());
            }
            int affectedRows = ps.executeUpdate();
            logger.info("User updated: " + user.getUsername());
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error updating user", e);
        }
        return false;
    }

    /**
     * Delete user
     */
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affectedRows = ps.executeUpdate();
            logger.info("User deleted with id: " + id);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error deleting user", e);
        }
        return false;
    }

    /**
     * Change password
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);
        if (user != null && PasswordUtil.verifyPassword(oldPassword, user.getPassword())) {
            String sql = "UPDATE users SET password = ? WHERE id = ?";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, PasswordUtil.hashPassword(newPassword));
                ps.setInt(2, userId);
                int affectedRows = ps.executeUpdate();
                logger.info("Password changed for user id: " + userId);
                return affectedRows > 0;
            } catch (SQLException e) {
                logger.error("Error changing password", e);
            }
        }
        return false;
    }

    /**
     * Map ResultSet to User object
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getString("role"));
        user.setActive(rs.getBoolean("active"));
        user.setCreatedAt(rs.getLong("created_at"));
        return user;
    }
}


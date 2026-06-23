package org.example.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Hash password using BCrypt
     */
    public static String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * Verify password against hashed password
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        try {
            return passwordEncoder.matches(password, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Generate a random salt-like string for additional security
     */
    public static String generateSalt() {
        return passwordEncoder.encode(String.valueOf(System.currentTimeMillis()));
    }
}


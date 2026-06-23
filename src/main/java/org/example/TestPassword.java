package org.example;

import org.example.security.PasswordUtil;

public class TestPassword {
    public static void main(String[] args) {
        System.out.println(
                PasswordUtil.hashPassword("user")
        );
    }
}
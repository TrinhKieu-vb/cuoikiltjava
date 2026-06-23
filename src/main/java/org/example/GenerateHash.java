package org.example;

import org.example.security.PasswordUtil;

public class GenerateHash {
    public static void main(String[] args) {
        System.out.println(
                PasswordUtil.hashPassword("admin")
        );
    }
}
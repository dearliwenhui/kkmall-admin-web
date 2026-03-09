package com.ab.kkmalladminweb.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具类
 * 用于生成 BCrypt 加密的密码
 *
 * @author KKMall
 * @since 2026-02-01
 */
public class PasswordGenerator {

    /**
     * 生成 BCrypt 加密密码
     *
     * @param rawPassword 明文密码
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(rawPassword);
    }

    /**
     * 验证密码是否匹配
     *
     * @param rawPassword     明文密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 主方法 - 用于生成密码
     */
    public static void main(String[] args) {
        boolean matches = PasswordGenerator.matches("admin123456", "$2a$10$3qJItGmN5vlKZ/W9bF7j9ecN0sV.V6nj8XGm6aD0QEIsZThMnkAmS");
        System.out.println("密码匹配: " + matches);

        // 生成常用密码的加密值
        String[] passwords = {"admin123456"};

        System.out.println("========================================");
        System.out.println("BCrypt 密码生成器");
        System.out.println("========================================\n");

        for (String password : passwords) {
            String encoded = encode(password);
            System.out.println("明文密码: " + password);
            System.out.println("加密密码: " + encoded);
            System.out.println();
        }
    }
}

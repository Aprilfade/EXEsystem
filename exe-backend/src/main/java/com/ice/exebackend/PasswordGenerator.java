package com.ice.exebackend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 您可以在这里放入任何您想设置的密码
        String rawPassword = "123456";

        String encodedPassword = encoder.encode(rawPassword);

        // 运行后，控制台会打印出加密后的密码字符串
        System.out.println("密码 '" + rawPassword + "' 加密后是: " + encodedPassword);
    }
}
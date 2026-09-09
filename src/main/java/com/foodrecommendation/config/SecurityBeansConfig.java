package com.foodrecommendation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// Chỉ dùng spring-security-crypto để hash password (BCrypt),
// KHÔNG bật Spring Security đầy đủ — không có filter chain,
// không ảnh hưởng tới các API hiện có.
@Configuration
public class SecurityBeansConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
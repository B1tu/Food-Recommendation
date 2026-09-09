package com.foodrecommendation.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodrecommendation.entity.User;
import com.foodrecommendation.repository.UserRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // POST /api/auth/login
    // Body: { "email": "...", "password": "..." }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Vui lòng nhập email và mật khẩu"));
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty() || userOpt.get().getPassword() == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Email hoặc mật khẩu không đúng"));
        }

        User user = userOpt.get();
        String stored = user.getPassword();
        boolean valid;

        if (isBcryptHash(stored)) {
            // Mật khẩu đã hash (tạo qua API đăng ký/guest của app) — so khớp bình thường.
            valid = passwordEncoder.matches(password, stored);
        } else {
            // Tài khoản có sẵn trong DB từ trước (tạo trực tiếp, không qua app) —
            // mật khẩu đang lưu dạng plaintext. So khớp trực tiếp, KHÔNG tự
            // nâng cấp sang hash (Ray muốn tạm giữ nguyên để còn xem lại được
            // password trong DB — sẽ bật lại nâng cấp tự động khi cần).
            valid = stored.equals(password);
        }

        if (!valid) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Email hoặc mật khẩu không đúng"));
        }

        user.setPassword(null); // không trả password về client
        return ResponseEntity.ok(user);
    }

    // BCrypt hash luôn có dạng $2a$/$2b$/$2y$ + cost factor + 53 ký tự salt+hash (60 ký tự tổng).
    private boolean isBcryptHash(String value) {
        return value != null && value.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }
}
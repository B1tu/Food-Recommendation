package com.foodrecommendation.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.foodrecommendation.entity.User;
import com.foodrecommendation.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Lấy tất cả User
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Lấy User theo ID
    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    // Thêm User (guest: không có email/password; user thật: có cả hai)
    public User createUser(User user) {
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new IllegalArgumentException("Email đã được sử dụng");
            }
        }
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    // Cập nhật User
    public User updateUser(Integer id, User userDetails) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        user.setPassword(userDetails.getPassword());
        user.setLocation(userDetails.getLocation());

        return userRepository.save(user);
    }

    // Xóa User
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }
}
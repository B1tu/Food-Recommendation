package com.foodrecommendation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foodrecommendation.entity.AIConversation;

public interface AIConversationRepository
        extends JpaRepository<AIConversation, Integer> {

    List<AIConversation> findByUserId(Integer userId);

    // 5 lượt hỏi-đáp gần nhất trong cùng 1 phiên chat (để AI nhớ ngữ cảnh
    // câu trước, không lấy toàn bộ lịch sử user tránh phình prompt quá lớn).
    List<AIConversation> findTop5BySessionIdOrderByCreatedAtDesc(String sessionId);
}
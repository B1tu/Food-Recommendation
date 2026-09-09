package com.foodrecommendation.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import com.foodrecommendation.dto.NearbyRestaurantDto;

@Entity
@Table(name = "AI_CONVERSATION")
public class AIConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversation_id")
    private Integer conversationId;

    // Field đơn giản — không dùng @ManyToOne User nữa
    // để tránh lỗi mapping khi frontend gửi userId dạng số
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "user_message", columnDefinition = "text")
    private String userMessage;

    @Column(name = "ai_response", columnDefinition = "text")
    private String aiResponse;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Nhóm nhiều lượt hỏi-đáp thành 1 "phiên chat" ở phía client
    // (client tự sinh UUID, gửi kèm mỗi lượt trong cùng phiên).
    // Có thể null với các dòng tạo trước khi thêm field này.
    @Column(name = "session_id", length = 64)
    private String sessionId;

    // Vị trí người dùng gửi kèm mỗi lượt chat (không lưu DB, chỉ dùng để
    // tìm nhà hàng gần đây cho lượt trả lời này — trình duyệt hỏi lại
    // quyền định vị mỗi phiên, không có cột tương ứng trong schema hiện tại).
    @Transient
    private Double latitude;

    @Transient
    private Double longitude;

    // Danh sách nhà hàng gần vị trí người dùng (nếu có) — trả về cùng
    // aiResponse để frontend hiển thị dạng thẻ (card), không lưu DB.
    @Transient
    private List<NearbyRestaurantDto> nearbyRestaurants;

    public AIConversation() {
    }

    public AIConversation(Integer userId, String userMessage,
                          String aiResponse, LocalDateTime createdAt) {
        this.userId = userId;
        this.userMessage = userMessage;
        this.aiResponse = aiResponse;
        this.createdAt = createdAt;
    }

    // Getters và Setters
    public Integer getConversationId() {
        return conversationId;
    }

    public void setConversationId(Integer conversationId) {
        this.conversationId = conversationId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getAiResponse() {
        return aiResponse;
    }

    public void setAiResponse(String aiResponse) {
        this.aiResponse = aiResponse;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public List<NearbyRestaurantDto> getNearbyRestaurants() {
        return nearbyRestaurants;
    }

    public void setNearbyRestaurants(List<NearbyRestaurantDto> nearbyRestaurants) {
        this.nearbyRestaurants = nearbyRestaurants;
    }
}
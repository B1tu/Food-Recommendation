package com.foodrecommendation.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "history")
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Integer historyId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "food_id")
    private Integer foodId;

    @Column(name = "restaurant_id")
    private Integer restaurantId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // =========================
    // Constructors
    // =========================

    public History() {
    }

    public History(
            Integer userId,
            Integer foodId,
            Integer restaurantId,
            LocalDateTime createdAt) {

        this.userId = userId;
        this.foodId = foodId;
        this.restaurantId = restaurantId;
        this.createdAt = createdAt;
    }

    // =========================
    // Getters and Setters
    // =========================

    public Integer getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Integer historyId) {
        this.historyId = historyId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getFoodId() {
        return foodId;
    }

    public void setFoodId(Integer foodId) {
        this.foodId = foodId;
    }

    public Integer getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
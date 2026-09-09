package com.foodrecommendation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "recommendation")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_id")
    private Integer recommendationId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "food_id", nullable = false)
    private Integer foodId;

    @Column(name = "score")
    private Double score;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    // =========================
    // Constructors
    // =========================

    public Recommendation() {
    }

    public Recommendation(
            Integer userId,
            Integer foodId,
            Double score,
            String reason) {

        this.userId = userId;
        this.foodId = foodId;
        this.score = score;
        this.reason = reason;
    }

    // =========================
    // Getters and Setters
    // =========================

    public Integer getRecommendationId() {
        return recommendationId;
    }

    public void setRecommendationId(Integer recommendationId) {
        this.recommendationId = recommendationId;
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

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
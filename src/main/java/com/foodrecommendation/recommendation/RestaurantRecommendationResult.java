package com.foodrecommendation.recommendation;

import java.io.Serializable;

public class RestaurantRecommendationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer restaurantId;
    private Double score;
    private String reason;

    public RestaurantRecommendationResult() {
    }

    public RestaurantRecommendationResult(Integer restaurantId, Double score, String reason) {
        this.restaurantId = restaurantId;
        this.score = score;
        this.reason = reason;
    }

    public Integer getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
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

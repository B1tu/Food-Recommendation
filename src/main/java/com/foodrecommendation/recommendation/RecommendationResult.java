package com.foodrecommendation.recommendation;

public class RecommendationResult {

    private Integer foodId;
    private double score;
    private String reason;

    public RecommendationResult() {
    }

    public RecommendationResult(Integer foodId, double score, String reason) {
        this.foodId = foodId;
        this.score = score;
        this.reason = reason;
    }

    public Integer getFoodId() {
        return foodId;
    }

    public void setFoodId(Integer foodId) {
        this.foodId = foodId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
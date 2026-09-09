package com.foodrecommendation.entity;

import java.io.Serializable;
import java.util.Objects;

public class RestaurantFoodId implements Serializable {

    private Integer restaurantId;
    private Integer foodId;

    // =========================
    // Constructors
    // =========================

    public RestaurantFoodId() {
    }

    public RestaurantFoodId(Integer restaurantId, Integer foodId) {
        this.restaurantId = restaurantId;
        this.foodId = foodId;
    }

    // =========================
    // Getters and Setters
    // =========================

    public Integer getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }

    public Integer getFoodId() {
        return foodId;
    }

    public void setFoodId(Integer foodId) {
        this.foodId = foodId;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;

        if (!(o instanceof RestaurantFoodId)) return false;

        RestaurantFoodId that = (RestaurantFoodId) o;

        return Objects.equals(restaurantId, that.restaurantId)
                && Objects.equals(foodId, that.foodId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(restaurantId, foodId);
    }
}
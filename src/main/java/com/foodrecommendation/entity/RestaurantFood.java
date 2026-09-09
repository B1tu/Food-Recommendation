package com.foodrecommendation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "restaurant_food")
@IdClass(RestaurantFoodId.class)
public class RestaurantFood {

    @Id
    @Column(name = "restaurant_id")
    private Integer restaurantId;

    @Id
    @Column(name = "food_id")
    private Integer foodId;

    @Column(name = "price")
    private Double price;

    @Column(name = "is_available")
    private Boolean isAvailable;

    // =========================
    // Constructors
    // =========================

    public RestaurantFood() {
    }

    public RestaurantFood(
            Integer restaurantId,
            Integer foodId,
            Double price,
            Boolean isAvailable) {

        this.restaurantId = restaurantId;
        this.foodId = foodId;
        this.price = price;
        this.isAvailable = isAvailable;
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
}
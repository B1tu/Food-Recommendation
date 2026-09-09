package com.foodrecommendation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "nutrition")
public class Nutrition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nutrition_id")
    private Integer nutritionId;

    @Column(name = "food_id", nullable = false)
    private Integer foodId;

    @Column(name = "calories")
    private Double calories;

    @Column(name = "protein")
    private Double protein;

    @Column(name = "fat")
    private Double fat;

    @Column(name = "carbohydrates")
    private Double carbohydrates;

    // =========================
    // Constructors
    // =========================

    public Nutrition() {
    }

    public Nutrition(
            Integer foodId,
            Double calories,
            Double protein,
            Double fat,
            Double carbohydrates) {

        this.foodId = foodId;
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.carbohydrates = carbohydrates;
    }

    // =========================
    // Getters and Setters
    // =========================

    public Integer getNutritionId() {
        return nutritionId;
    }

    public void setNutritionId(Integer nutritionId) {
        this.nutritionId = nutritionId;
    }

    public Integer getFoodId() {
        return foodId;
    }

    public void setFoodId(Integer foodId) {
        this.foodId = foodId;
    }

    public Double getCalories() {
        return calories;
    }

    public void setCalories(Double calories) {
        this.calories = calories;
    }

    public Double getProtein() {
        return protein;
    }

    public void setProtein(Double protein) {
        this.protein = protein;
    }

    public Double getFat() {
        return fat;
    }

    public void setFat(Double fat) {
        this.fat = fat;
    }

    public Double getCarbohydrates() {
        return carbohydrates;
    }

    public void setCarbohydrates(Double carbohydrates) {
        this.carbohydrates = carbohydrates;
    }
}
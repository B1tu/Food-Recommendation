package com.foodrecommendation.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "user_profile",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "user_id")
    }
)
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Integer profileId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Integer userId;

    @Column(name = "age")
    private Integer age;

    @Column(name = "preferences", columnDefinition = "TEXT")
    private String preferences;

    @Column(name = "dietary_preferences", columnDefinition = "TEXT")
    private String dietaryPreferences;

    @Column(name = "budget", precision = 18, scale = 2)
    private BigDecimal budget;

    // =========================
    // Constructors
    // =========================
    
    public UserProfile() {
    }

    // =========================
    // Getters and Setters
    // =========================

    public Integer getProfileId() {
        return profileId;
    }

    public void setProfileId(Integer profileId) {
        this.profileId = profileId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getPreferences() {
        return preferences;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }

    public String getDietaryPreferences() {
        return dietaryPreferences;
    }

    public void setDietaryPreferences(String dietaryPreferences) {
        this.dietaryPreferences = dietaryPreferences;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }
}
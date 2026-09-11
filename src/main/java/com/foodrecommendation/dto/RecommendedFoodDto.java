package com.foodrecommendation.dto;

public class RecommendedFoodDto {

    private Integer foodId;
    private String name;
    private Double price;
    private String restaurantName;
    private String restaurantAddress;
    private String openingHours;

    public RecommendedFoodDto() {
    }

    public RecommendedFoodDto(Integer foodId, String name, Double price,
                               String restaurantName, String restaurantAddress, String openingHours) {
        this.foodId = foodId;
        this.name = name;
        this.price = price;
        this.restaurantName = restaurantName;
        this.restaurantAddress = restaurantAddress;
        this.openingHours = openingHours;
    }

    public Integer getFoodId() {
        return foodId;
    }

    public void setFoodId(Integer foodId) {
        this.foodId = foodId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getRestaurantAddress() {
        return restaurantAddress;
    }

    public void setRestaurantAddress(String restaurantAddress) {
        this.restaurantAddress = restaurantAddress;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }
}
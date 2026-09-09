package com.foodrecommendation.dto;

public class NearbyRestaurantDto {

    private Integer restaurantId;
    private String name;
    private Double rating;
    private Double distanceKm;
    private String address;
    private String openingHours;

    public NearbyRestaurantDto() {
    }

    public NearbyRestaurantDto(Integer restaurantId, String name, Double rating,
                                Double distanceKm, String address, String openingHours) {
        this.restaurantId = restaurantId;
        this.name = name;
        this.rating = rating;
        this.distanceKm = distanceKm;
        this.address = address;
        this.openingHours = openingHours;
    }

    public Integer getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }
}
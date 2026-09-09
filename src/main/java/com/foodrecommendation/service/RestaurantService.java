package com.foodrecommendation.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.foodrecommendation.entity.Restaurant;
import com.foodrecommendation.repository.RestaurantRepository;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    // Lấy tất cả nhà hàng
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    // Lấy nhà hàng theo ID
    public Optional<Restaurant> getRestaurantById(Integer restaurantId) {
        return restaurantRepository.findById(restaurantId);
    }

    // Thêm nhà hàng
    public Restaurant createRestaurant(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    // Cập nhật nhà hàng
    public Restaurant updateRestaurant(
            Integer restaurantId,
            Restaurant restaurantDetails) {

        Restaurant restaurant = restaurantRepository
                .findById(restaurantId)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy nhà hàng với ID: "
                                + restaurantId
                ));

        restaurant.setName(restaurantDetails.getName());
        restaurant.setAddress(restaurantDetails.getAddress());
        restaurant.setLatitude(restaurantDetails.getLatitude());
        restaurant.setLongitude(restaurantDetails.getLongitude());
        restaurant.setRating(restaurantDetails.getRating());
        restaurant.setOpeningHours(
                restaurantDetails.getOpeningHours()
        );

        return restaurantRepository.save(restaurant);
    }

    // Xóa nhà hàng
    public void deleteRestaurant(Integer restaurantId) {

        if (!restaurantRepository.existsById(restaurantId)) {
            throw new RuntimeException(
                    "Không tìm thấy nhà hàng với ID: "
                            + restaurantId
            );
        }

        restaurantRepository.deleteById(restaurantId);
    }
}
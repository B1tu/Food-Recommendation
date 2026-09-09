package com.foodrecommendation.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.foodrecommendation.entity.RestaurantFood;
import com.foodrecommendation.entity.RestaurantFoodId;
import com.foodrecommendation.repository.RestaurantFoodRepository;

@Service
public class RestaurantFoodService {

    private final RestaurantFoodRepository repository;

    public RestaurantFoodService(
            RestaurantFoodRepository repository) {

        this.repository = repository;
    }

    // Lấy toàn bộ quan hệ nhà hàng - món ăn
    public List<RestaurantFood> getAll() {
        return repository.findAll();
    }

    // Tìm theo khóa chính kép
    public Optional<RestaurantFood> getById(
            Integer restaurantId,
            Integer foodId) {

        RestaurantFoodId id =
                new RestaurantFoodId(
                        restaurantId,
                        foodId
                );

        return repository.findById(id);
    }

    // Lấy tất cả món của một nhà hàng
    public List<RestaurantFood> getByRestaurantId(
            Integer restaurantId) {

        return repository.findByRestaurantId(restaurantId);
    }

    // Lấy tất cả nhà hàng bán một món
    public List<RestaurantFood> getByFoodId(
            Integer foodId) {

        return repository.findByFoodId(foodId);
    }

    // Thêm món vào nhà hàng
    public RestaurantFood create(
            RestaurantFood restaurantFood) {

        return repository.save(restaurantFood);
    }

    // Xóa món khỏi nhà hàng
    public void delete(
            Integer restaurantId,
            Integer foodId) {

        RestaurantFoodId id =
                new RestaurantFoodId(
                        restaurantId,
                        foodId
                );

        repository.deleteById(id);
    }
}
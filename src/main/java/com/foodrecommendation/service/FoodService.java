package com.foodrecommendation.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.foodrecommendation.entity.Food;
import com.foodrecommendation.repository.FoodRepository;

@Service
public class FoodService {

    private final FoodRepository foodRepository;

    public FoodService(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    // Lấy tất cả món ăn
    public List<Food> getAllFoods() {
        return foodRepository.findAll();
    }

    // Lấy món ăn theo ID
    public Optional<Food> getFoodById(Integer foodId) {
        return foodRepository.findById(foodId);
    }

    // Thêm món ăn
    public Food createFood(Food food) {
        return foodRepository.save(food);
    }

    // Cập nhật món ăn
    public Food updateFood(Integer foodId, Food foodDetails) {

        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy món ăn với ID: " + foodId
                ));

        food.setName(foodDetails.getName());
        food.setDescription(foodDetails.getDescription());
        food.setCuisineType(foodDetails.getCuisineType());
        food.setPrice(foodDetails.getPrice());
        food.setImageUrl(foodDetails.getImageUrl());

        return foodRepository.save(food);
    }

    // Xóa món ăn
    public void deleteFood(Integer foodId) {

        if (!foodRepository.existsById(foodId)) {
            throw new RuntimeException(
                    "Không tìm thấy món ăn với ID: " + foodId
            );
        }

        foodRepository.deleteById(foodId);
    }
}
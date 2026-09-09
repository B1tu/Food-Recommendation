package com.foodrecommendation.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.foodrecommendation.entity.Nutrition;
import com.foodrecommendation.repository.NutritionRepository;

@Service
public class NutritionService {

    private final NutritionRepository repository;

    public NutritionService(NutritionRepository repository) {
        this.repository = repository;
    }

    // Lấy toàn bộ dữ liệu dinh dưỡng
    public List<Nutrition> getAll() {
        return repository.findAll();
    }

    // Lấy dinh dưỡng theo ID
    public Optional<Nutrition> getById(Integer nutritionId) {
        return repository.findById(nutritionId);
    }

    // Lấy dinh dưỡng theo món ăn
    public Optional<Nutrition> getByFoodId(Integer foodId) {
        return repository.findByFoodId(foodId);
    }

    // Thêm dữ liệu dinh dưỡng
    public Nutrition create(Nutrition nutrition) {
        return repository.save(nutrition);
    }

    // Cập nhật
    public Nutrition update(
            Integer nutritionId,
            Nutrition nutrition) {

        Nutrition existing =
                repository.findById(nutritionId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Không tìm thấy Nutrition"
                                )
                        );

        existing.setFoodId(nutrition.getFoodId());
        existing.setCalories(nutrition.getCalories());
        existing.setProtein(nutrition.getProtein());
        existing.setFat(nutrition.getFat());
        existing.setCarbohydrates(
                nutrition.getCarbohydrates()
        );

        return repository.save(existing);
    }

    // Xóa
    public void delete(Integer nutritionId) {
        repository.deleteById(nutritionId);
    }
}
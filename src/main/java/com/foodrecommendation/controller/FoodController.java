package com.foodrecommendation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodrecommendation.entity.Food;
import com.foodrecommendation.service.FoodService;

@RestController
@RequestMapping("/api/foods")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    // GET /api/foods
    @GetMapping
    public List<Food> getAllFoods() {
        return foodService.getAllFoods();
    }

    // GET /api/foods/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Food> getFoodById(
            @PathVariable Integer id) {

        return foodService.getFoodById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/foods
    @PostMapping
    public ResponseEntity<Food> createFood(
            @RequestBody Food food) {

        Food savedFood = foodService.createFood(food);

        return ResponseEntity.ok(savedFood);
    }

    // PUT /api/foods/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Food> updateFood(
            @PathVariable Integer id,
            @RequestBody Food food) {

        try {
            Food updatedFood = foodService.updateFood(id, food);
            return ResponseEntity.ok(updatedFood);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/foods/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFood(
            @PathVariable Integer id) {

        try {
            foodService.deleteFood(id);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
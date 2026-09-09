package com.foodrecommendation.controller;

import com.foodrecommendation.entity.Nutrition;
import com.foodrecommendation.service.NutritionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nutrition")
public class NutritionController {

    private final NutritionService service;

    public NutritionController(NutritionService service) {
        this.service = service;
    }

    // GET /api/nutrition
    @GetMapping
    public List<Nutrition> getAll() {
        return service.getAll();
    }

    // GET /api/nutrition/1
    @GetMapping("/{nutritionId}")
    public ResponseEntity<Nutrition> getById(
            @PathVariable Integer nutritionId) {

        return service.getById(nutritionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/nutrition/food/1
    @GetMapping("/food/{foodId}")
    public ResponseEntity<Nutrition> getByFoodId(
            @PathVariable Integer foodId) {

        return service.getByFoodId(foodId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/nutrition
    @PostMapping
    public ResponseEntity<Nutrition> create(
            @RequestBody Nutrition nutrition) {

        return ResponseEntity.ok(
                service.create(nutrition)
        );
    }

    // PUT /api/nutrition/1
    @PutMapping("/{nutritionId}")
    public ResponseEntity<Nutrition> update(
            @PathVariable Integer nutritionId,
            @RequestBody Nutrition nutrition) {

        return ResponseEntity.ok(
                service.update(
                        nutritionId,
                        nutrition
                )
        );
    }

    // DELETE /api/nutrition/1
    @DeleteMapping("/{nutritionId}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer nutritionId) {

        service.delete(nutritionId);

        return ResponseEntity.noContent().build();
    }
}
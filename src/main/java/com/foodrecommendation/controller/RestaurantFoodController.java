package com.foodrecommendation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodrecommendation.entity.RestaurantFood;
import com.foodrecommendation.service.RestaurantFoodService;

@RestController
@RequestMapping("/api/restaurant-foods")
public class RestaurantFoodController {

    private final RestaurantFoodService service;

    public RestaurantFoodController(
            RestaurantFoodService service) {

        this.service = service;
    }

    // GET /api/restaurant-foods
    @GetMapping
    public List<RestaurantFood> getAll() {
        return service.getAll();
    }

    // GET /api/restaurant-foods/restaurant/1
    @GetMapping("/restaurant/{restaurantId}")
    public List<RestaurantFood> getByRestaurant(
            @PathVariable Integer restaurantId) {

        return service.getByRestaurantId(restaurantId);
    }

    // GET /api/restaurant-foods/food/1
    @GetMapping("/food/{foodId}")
    public List<RestaurantFood> getByFood(
            @PathVariable Integer foodId) {

        return service.getByFoodId(foodId);
    }

    // POST /api/restaurant-foods
    @PostMapping
    public ResponseEntity<RestaurantFood> create(
            @RequestBody RestaurantFood restaurantFood) {

        return ResponseEntity.ok(
                service.create(restaurantFood)
        );
    }

    // DELETE
    // /api/restaurant-foods/restaurant/1/food/2
    @DeleteMapping("/restaurant/{restaurantId}/food/{foodId}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer restaurantId,
            @PathVariable Integer foodId) {

        service.delete(restaurantId, foodId);

        return ResponseEntity.noContent().build();
    }
}
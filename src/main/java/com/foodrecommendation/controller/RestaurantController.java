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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.foodrecommendation.entity.Restaurant;
import com.foodrecommendation.recommendation.RestaurantRecommendationResult;
import com.foodrecommendation.service.RestaurantRecommendationService;
import com.foodrecommendation.service.RestaurantService;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final RestaurantRecommendationService recommendationService;

    public RestaurantController(
            RestaurantService restaurantService,
            RestaurantRecommendationService recommendationService) {
        this.restaurantService = restaurantService;
        this.recommendationService = recommendationService;
    }

    // GET /api/restaurants
    @GetMapping
    public List<Restaurant> getAllRestaurants() {
        return restaurantService.getAllRestaurants();
    }

    // GET /api/restaurants/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getRestaurantById(
            @PathVariable Integer id) {

        return restaurantService
                .getRestaurantById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/restaurants
    @PostMapping
    public ResponseEntity<Restaurant> createRestaurant(
            @RequestBody Restaurant restaurant) {

        Restaurant savedRestaurant =
                restaurantService.createRestaurant(restaurant);

        return ResponseEntity.ok(savedRestaurant);
    }

    // PUT /api/restaurants/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Restaurant> updateRestaurant(
            @PathVariable Integer id,
            @RequestBody Restaurant restaurant) {

        try {

            Restaurant updatedRestaurant =
                    restaurantService.updateRestaurant(
                            id,
                            restaurant
                    );

            return ResponseEntity.ok(updatedRestaurant);

        } catch (RuntimeException e) {

            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/restaurants/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(
            @PathVariable Integer id) {

        try {

            restaurantService.deleteRestaurant(id);

            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {

            return ResponseEntity.notFound().build();
        }
    }

    // POST /api/restaurants/recommend
    // Gợi ý nhà hàng theo vị trí, user, bán kính
    // Request params: userId (optional), lat (optional), lng (optional), radiusKm (optional)
    @PostMapping("/recommend")
    public ResponseEntity<List<RestaurantRecommendationResult>> recommendRestaurants(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double radiusKm) {

        List<RestaurantRecommendationResult> results =
                recommendationService.recommendRestaurants(userId, lat, lng, radiusKm);

        return ResponseEntity.ok(results);
    }
}

package com.foodrecommendation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.foodrecommendation.entity.RestaurantFood;
import com.foodrecommendation.entity.RestaurantFoodId;

@Repository
public interface RestaurantFoodRepository
        extends JpaRepository<RestaurantFood, RestaurantFoodId> {

    List<RestaurantFood> findByRestaurantId(Integer restaurantId);

    List<RestaurantFood> findByFoodId(Integer foodId);

}
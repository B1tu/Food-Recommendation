package com.foodrecommendation.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.foodrecommendation.entity.Nutrition;

@Repository
public interface NutritionRepository
        extends JpaRepository<Nutrition, Integer> {

    Optional<Nutrition> findByFoodId(Integer foodId);
}
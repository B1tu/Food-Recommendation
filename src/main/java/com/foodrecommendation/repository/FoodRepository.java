package com.foodrecommendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.foodrecommendation.entity.Food;

@Repository
public interface FoodRepository extends JpaRepository<Food, Integer> {

}
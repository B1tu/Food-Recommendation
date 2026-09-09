package com.foodrecommendation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.foodrecommendation.entity.Recommendation;

@Repository
public interface RecommendationRepository
        extends JpaRepository<Recommendation, Integer> {

    List<Recommendation> findByUserIdOrderByScoreDesc(
            Integer userId
    );

    void deleteByUserId(Integer userID);
}
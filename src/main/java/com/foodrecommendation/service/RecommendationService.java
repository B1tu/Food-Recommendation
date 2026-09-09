package com.foodrecommendation.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodrecommendation.entity.Food;
import com.foodrecommendation.entity.History;
import com.foodrecommendation.entity.Nutrition;
import com.foodrecommendation.entity.Recommendation;
import com.foodrecommendation.entity.UserProfile;
import com.foodrecommendation.recommendation.RecommendationEngine;
import com.foodrecommendation.recommendation.RecommendationResult;
import com.foodrecommendation.repository.FoodRepository;
import com.foodrecommendation.repository.HistoryRepository;
import com.foodrecommendation.repository.NutritionRepository;
import com.foodrecommendation.repository.RecommendationRepository;
import com.foodrecommendation.repository.UserProfileRepository;

@Service
public class RecommendationService {

    private final RecommendationRepository repository;
    private final FoodRepository foodRepository;
    private final NutritionRepository nutritionRepository;
    private final HistoryRepository historyRepository;
    private final UserProfileRepository userProfileRepository;
    private final RecommendationEngine recommendationEngine;

    public RecommendationService(
            RecommendationRepository repository,
            FoodRepository foodRepository,
            NutritionRepository nutritionRepository,
            HistoryRepository historyRepository,
            UserProfileRepository userProfileRepository,
            RecommendationEngine recommendationEngine) {

        this.repository = repository;
        this.foodRepository = foodRepository;
        this.nutritionRepository = nutritionRepository;
        this.historyRepository = historyRepository;
        this.userProfileRepository = userProfileRepository;
        this.recommendationEngine = recommendationEngine;
    }

    // ==========================================
    // GET ALL
    // ==========================================

    public List<Recommendation> getAll() {
        return repository.findAll();
    }

    // ==========================================
    // GET BY ID
    // ==========================================

    public Optional<Recommendation> getById(
            Integer recommendationId) {

        return repository.findById(recommendationId);
    }

    // ==========================================
    // GET BY USER
    // ==========================================

    public List<Recommendation> getByUserId(
            Integer userId) {

        return repository
                .findByUserIdOrderByScoreDesc(userId);
    }

    // ==========================================
    // CREATE
    // ==========================================

    public Recommendation create(
            Recommendation recommendation) {

        return repository.save(recommendation);
    }

    // ==========================================
    // UPDATE
    // ==========================================

    public Recommendation update(
            Integer recommendationId,
            Recommendation recommendation) {

        Recommendation existing =
                repository.findById(recommendationId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Không tìm thấy recommendation"
                                )
                        );

        existing.setUserId(
                recommendation.getUserId()
        );

        existing.setFoodId(
                recommendation.getFoodId()
        );

        existing.setScore(
                recommendation.getScore()
        );

        existing.setReason(
                recommendation.getReason()
        );

        return repository.save(existing);
    }

    // ==========================================
    // DELETE BY ID
    // ==========================================

    public void delete(Integer recommendationId) {

        repository.deleteById(recommendationId);
    }

    // ==========================================
    // DELETE ALL RECOMMENDATION OF USER
    // ==========================================

    public void deleteByUserId(Integer userId) {

        repository.deleteByUserId(userId);
    }

    // ==========================================
    // REFRESH RECOMMENDATION
    // ==========================================
    @Transactional
    public List<Recommendation> refreshRecommendations(
            Integer userId) {

        // --------------------------------------
        // 1. Lấy UserProfile
        // --------------------------------------

        UserProfile profile =
                userProfileRepository.findByUserId(userId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Không tìm thấy UserProfile của user: "
                                                + userId
                                )
                        );

        // --------------------------------------
        // 2. Lấy toàn bộ món ăn
        // --------------------------------------

        List<Food> foods =
                foodRepository.findAll();

        // --------------------------------------
        // 3. Lấy History của user
        // --------------------------------------

        List<History> histories =
                historyRepository
                        .findByUserIdOrderByCreatedAtDesc(userId);

        // --------------------------------------
        // 4. Chuyển History thành Set foodId
        // --------------------------------------

        Map<Integer, Integer> historyFoodCounts =
                new HashMap<>();

        for (History history : histories) {

                if (history.getFoodId() != null) {

                        historyFoodCounts.merge(
                                history.getFoodId(),
                                1,
                                Integer::sum
                        );
                }
        }

        // --------------------------------------
        // 5. Xóa recommendation cũ
        // --------------------------------------

        repository.deleteByUserId(userId);

        // --------------------------------------
        // 6. Tính recommendation mới
        // --------------------------------------

        List<Recommendation> newRecommendations =
                new ArrayList<>();

        for (Food food : foods) {

            // Lấy Nutrition của món ăn
            Nutrition nutrition = nutritionRepository
                            .findByFoodId(food.getFoodId())
                            .orElse(null);

            // Recommendation Engine tính điểm
            RecommendationResult result =
                    recommendationEngine.calculateScore(
                            food,
                            nutrition,
                            profile,
                            historyFoodCounts
                    );

            // ----------------------------------
            // Tạo Recommendation Entity
            // ----------------------------------

            Recommendation recommendation =
                    new Recommendation();

            recommendation.setUserId(userId);

            recommendation.setFoodId(
                    result.getFoodId()
            );

            recommendation.setScore(
                    result.getScore()
            );

            recommendation.setReason(
                    result.getReason()
            );

            newRecommendations.add(
                    recommendation
            );
        }

        // --------------------------------------
        // 7. Lưu recommendation mới vào DB
        // --------------------------------------

        return repository.saveAll(
                newRecommendations
        );
    }
}
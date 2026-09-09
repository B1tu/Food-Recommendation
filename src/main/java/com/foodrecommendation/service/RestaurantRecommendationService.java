package com.foodrecommendation.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodrecommendation.entity.Food;
import com.foodrecommendation.entity.Restaurant;
import com.foodrecommendation.entity.RestaurantFood;
import com.foodrecommendation.entity.UserProfile;
import com.foodrecommendation.recommendation.RestaurantRecommendationEngine;
import com.foodrecommendation.recommendation.RestaurantRecommendationResult;
import com.foodrecommendation.repository.FoodRepository;
import com.foodrecommendation.repository.RestaurantFoodRepository;
import com.foodrecommendation.repository.RestaurantRepository;
import com.foodrecommendation.repository.UserProfileRepository;

@Service
public class RestaurantRecommendationService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantFoodRepository restaurantFoodRepository;
    private final FoodRepository foodRepository;
    private final UserProfileRepository userProfileRepository;
    private final RestaurantRecommendationEngine recommendationEngine;

    public RestaurantRecommendationService(
            RestaurantRepository restaurantRepository,
            RestaurantFoodRepository restaurantFoodRepository,
            FoodRepository foodRepository,
            UserProfileRepository userProfileRepository,
            RestaurantRecommendationEngine recommendationEngine) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantFoodRepository = restaurantFoodRepository;
        this.foodRepository = foodRepository;
        this.userProfileRepository = userProfileRepository;
        this.recommendationEngine = recommendationEngine;
    }

    /**
     * Gợi ý nhà hàng cho user dựa trên vị trí, sở thích, ngân sách.
     *
     * @param userId      ID người dùng (để lấy profile)
     * @param userLat     Vĩ độ người dùng (nullable)
     * @param userLng     Kinh độ người dùng (nullable)
     * @param radiusKm    Bán kính tìm kiếm (km) — null = không giới hạn
     * @return Danh sách RestaurantRecommendationResult sắp xếp theo score
     */
    @Transactional(readOnly = true)
    public List<RestaurantRecommendationResult> recommendRestaurants(
            Integer userId,
            Double userLat,
            Double userLng,
            Double radiusKm) {

        // 1. Lấy UserProfile
        Optional<UserProfile> profileOpt = (userId != null)
                ? userProfileRepository.findByUserId(userId)
                : Optional.empty();
        UserProfile profile = profileOpt.orElse(null);

        // 2. Lấy tất cả nhà hàng
        List<Restaurant> allRestaurants;
        if (radiusKm != null && userLat != null && userLng != null) {
            allRestaurants = restaurantRepository.findByDistance(userLat, userLng, radiusKm);
        } else {
            allRestaurants = restaurantRepository.findAll();
        }

        // 3. Lấy tất cả món ăn
        List<Food> allFoods = foodRepository.findAll();

        // 4. Map foodId → Food
        Map<Integer, Food> foodMap = allFoods.stream()
                .collect(Collectors.toMap(Food::getFoodId, f -> f, (a, b) -> a));

        // 5. Lấy toàn bộ RestaurantFood (liên kết nhà hàng - món ăn)
        List<RestaurantFood> allRestaurantFoods = restaurantFoodRepository.findAll();

        // 6. Gom món theo nhà hàng
        Map<Integer, List<RestaurantFood>> restaurantFoodsMap = allRestaurantFoods.stream()
                .collect(Collectors.groupingBy(RestaurantFood::getRestaurantId));

        // 7. Tính điểm cho từng nhà hàng
        List<RestaurantRecommendationResult> results = new ArrayList<>();

        for (Restaurant restaurant : allRestaurants) {
            List<RestaurantFood> rFoods = restaurantFoodsMap.getOrDefault(
                    restaurant.getRestaurantId(),
                    List.of()
            );

            RestaurantRecommendationResult result = recommendationEngine.calculateScore(
                    restaurant,
                    userLat,
                    userLng,
                    profile,
                    rFoods,
                    foodMap
            );

            results.add(result);
        }

        // 8. Sắp xếp theo score giảm dần
        results.sort(Comparator.comparingDouble(RestaurantRecommendationResult::getScore).reversed());

        return results;
    }
}

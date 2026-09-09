package com.foodrecommendation.recommendation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.springframework.stereotype.Component;

import com.foodrecommendation.entity.Food;
import com.foodrecommendation.entity.Restaurant;
import com.foodrecommendation.entity.RestaurantFood;
import com.foodrecommendation.entity.UserProfile;

@Component
public class RestaurantRecommendationEngine {

    // Keywords đánh dấu món không phù hợp với người ăn chay
    private static final Set<String> NON_VEGETARIAN_KEYWORDS = new HashSet<>();
    static {
        NON_VEGETARIAN_KEYWORDS.add("thịt");
        NON_VEGETARIAN_KEYWORDS.add("bò");
        NON_VEGETARIAN_KEYWORDS.add("heo");
        NON_VEGETARIAN_KEYWORDS.add("gà");
        NON_VEGETARIAN_KEYWORDS.add("hải sản");
        NON_VEGETARIAN_KEYWORDS.add("tôm");
        NON_VEGETARIAN_KEYWORDS.add("cua");
        NON_VEGETARIAN_KEYWORDS.add("mực");
        NON_VEGETARIAN_KEYWORDS.add("sushi");
        NON_VEGETARIAN_KEYWORDS.add("nướng");
        NON_VEGETARIAN_KEYWORDS.add("sườn");
        NON_VEGETARIAN_KEYWORDS.add("gà rán");
        NON_VEGETARIAN_KEYWORDS.add("burger");
        NON_VEGETARIAN_KEYWORDS.add("bít tết");
        NON_VEGETARIAN_KEYWORDS.add("bò bít");
        NON_VEGETARIAN_KEYWORDS.add("nấu chay"); // the flip side → positive
        NON_VEGETARIAN_KEYWORDS.add("chay");     // positive → handle separately
    }

    /**
     * Tính điểm gợi ý nhà hàng.
     *
     * @param restaurant   Nhà hàng cần đánh giá
     * @param userLat      Vĩ độ người dùng (null nếu chưa có)
     * @param userLng      Kinh độ người dùng (null nếu chưa có)
     * @param profile      Hồ sơ người dùng (sở thích, budget, dietary)
     * @param restaurantFoods Danh sách món ăn của nhà hàng này
     * @return Kết quả recommendation với score và reason
     */
    public RestaurantRecommendationResult calculateScore(
            Restaurant restaurant,
            Double userLat,
            Double userLng,
            UserProfile profile,
            List<RestaurantFood> restaurantFoods,
            Map<Integer, Food> foodMap) {

        double score = 0.0;
        StringBuilder reason = new StringBuilder();

        // =============================================
        // 1. RATING - tối đa 30 điểm
        // =============================================
        double ratingScore = calculateRatingScore(restaurant.getRating());
        score += ratingScore;
        if (ratingScore >= 25) {
            reason.append("Nhà hàng có đánh giá rất cao. ");
        } else if (ratingScore >= 15) {
            reason.append("Nhà hàng có đánh giá tốt. ");
        }

        // =============================================
        // 2. DISTANCE - tối đa 25 điểm
        // =============================================
        if (userLat != null && userLng != null &&
            restaurant.getLatitude() != null && restaurant.getLongitude() != null) {

            double distanceScore = calculateDistanceScore(
                    userLat, userLng,
                    restaurant.getLatitude(), restaurant.getLongitude()
            );
            score += distanceScore;

            double distanceKm = calculateDistance(
                    userLat, userLng,
                    restaurant.getLatitude(), restaurant.getLongitude()
            );

            if (distanceKm <= 2.0) {
                reason.append("Nằm gần bạn (chỉ khoảng ").append(String.format("%.1f", distanceKm)).append(" km). ");
            } else if (distanceKm <= 5.0) {
                reason.append("Không xa bạn lắm (khoảng ").append(String.format("%.1f", distanceKm)).append(" km). ");
            } else {
                reason.append("Cách bạn khoảng ").append(String.format("%.1f", distanceKm)).append(" km. ");
            }
        } else {
            // Không có vị trí → tính điểm mặc định
            score += 10;
            reason.append("Vị trí không rõ ràng, dùng mặc định. ");
        }

        // =============================================
        // 3. CUISINE + DIETARY MATCH - tối đa 20 điểm
        // =============================================
        if (profile != null && profile.getPreferences() != null && !profile.getPreferences().isBlank()) {
            double cuisineScore = calculateCuisineScore(restaurantFoods, foodMap, profile.getPreferences());
            score += cuisineScore;

            if (cuisineScore >= 15) {
                reason.append("Có nhiều món phù hợp sở thích bạn. ");
            } else if (cuisineScore > 0) {
                reason.append("Có một số món bạn có thể thích. ");
            }

            // Dietary filtering: nếu user ăn chay, giảm điểm cho nhà hàng không có món chay
            if (profile.getDietaryPreferences() != null && !profile.getDietaryPreferences().isBlank()) {
                double dietaryScore = calculateDietaryScore(restaurantFoods, foodMap, profile.getDietaryPreferences());
                score += dietaryScore;

                if (dietaryScore <= -15) {
                    reason.append("Nhà hàng này không phù hợp với chế độ ăn của bạn. ");
                } else if (dietaryScore < 0) {
                    reason.append("Có một số hạn chế về chế độ ăn. ");
                } else if (dietaryScore >= 10) {
                    reason.append("Rất phù hợp với chế độ ăn của bạn. ");
                }
            }
        } else {
            score += 10; // Mặc định
            reason.append("Không có sở thích cụ thể, dùng mặc định. ");
        }

        // =============================================
        // 4. BUDGET - tối đa 15 điểm
        // =============================================
        if (profile != null && profile.getBudget() != null && restaurantFoods != null && !restaurantFoods.isEmpty()) {
            double budgetScore = calculateBudgetScore(restaurantFoods, profile.getBudget());
            score += budgetScore;

            if (budgetScore >= 12) {
                reason.append("Giá cả rất phù hợp ngân sách. ");
            } else if (budgetScore >= 8) {
                reason.append("Giá cả khá phù hợp. ");
            } else if (budgetScore > 0) {
                reason.append("Giá có vượt một chút. ");
            }
        } else {
            score += 5;
            reason.append("Không có thông tin ngân sách, dùng mặc định. ");
        }

        // =============================================
        // 5. OPENING HOURS - tối đa 10 điểm
        // =============================================
        if (restaurant.getOpeningHours() != null && !restaurant.getOpeningHours().isBlank()) {
            score += 10;
            reason.append("Có thông tin giờ mở cửa. ");
        } else {
            score += 3;
            reason.append("Chưa rõ giờ mở cửa. ");
        }

        // =============================================
        // 6. VARIETY - tối đa 10 điểm (số lượng món)
        // =============================================
        int varietyScore = calculateVarietyScore(restaurantFoods);
        score += varietyScore;
        if (varietyScore >= 8) {
            reason.append("Nhà hàng có nhiều lựa chọn món ăn. ");
        } else if (varietyScore >= 5) {
            reason.append("Có đủ loại món ăn. ");
        }

        // Giới hạn 0-100
        score = Math.max(0.0, Math.min(score, 100.0));

        return new RestaurantRecommendationResult(
                restaurant.getRestaurantId(),
                score,
                reason.toString().trim()
        );
    }

    // ----- RATING (max 30) -----

    private double calculateRatingScore(Double rating) {
        if (rating == null) return 0;
        if (rating >= 4.5) return 30;
        if (rating >= 4.0) return 25;
        if (rating >= 3.5) return 20;
        if (rating >= 3.0) return 15;
        return 10;
    }

    // ----- DISTANCE (max 25) -----

    private double calculateDistanceScore(Double userLat, Double userLng,
                                           Double rLat, Double rLng) {
        double distance = calculateDistance(userLat, userLng, rLat, rLng);
        if (distance <= 1.0) return 25;
        if (distance <= 2.0) return 22;
        if (distance <= 3.0) return 18;
        if (distance <= 5.0) return 14;
        if (distance <= 10.0) return 10;
        return 5;
    }

    /**
     * Tính khoảng cách km giữa 2 điểm (Haversine).
     */
    private double calculateDistance(Double lat1, Double lng1,
                                     Double lat2, Double lng2) {
        double R = 6371.0; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2.0) * Math.sin(dLng / 2.0);
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return R * c;
    }

    // ----- CUISINE + DIETARY (max 20) -----

    private double calculateCuisineScore(List<RestaurantFood> restaurantFoods,
                                          Map<Integer, Food> foodMap,
                                          String preferences) {
        if (restaurantFoods == null || restaurantFoods.isEmpty() || preferences == null || preferences.isBlank()) {
            return 0;
        }

        String pref = preferences.toLowerCase().trim();
        int matchedCount = 0;

        for (RestaurantFood rf : restaurantFoods) {
            Food food = foodMap.get(rf.getFoodId());
            if (food != null) {
                if (containsKeyword(pref, food.getName()) ||
                    containsKeyword(pref, food.getCuisineType()) ||
                    containsKeyword(pref, food.getDescription())) {
                    matchedCount++;
                }
            }
        }

        double ratio = (double) matchedCount / restaurantFoods.size();
        if (ratio >= 0.5) return 20;
        if (ratio >= 0.3) return 15;
        if (ratio >= 0.1) return 10;
        return Math.min((int)(ratio * 20), 5);
    }

    /**
     * Tính điểm dietary: kiểm tra house có phù hợp với chế độ ăn của user không.
     * Trả về điểm từ -20 (không phù hợp) đến +20 (rất phù hợp).
     *
     * Đặc biệt xử lý vegetarian: nếu dietary có "chay", tính tỷ lệ món chay trong nhà hàng.
     */
    private double calculateDietaryScore(List<RestaurantFood> restaurantFoods,
                                          Map<Integer, Food> foodMap,
                                          String dietaryPreferences) {
        if (restaurantFoods == null || restaurantFoods.isEmpty() || dietaryPreferences == null || dietaryPreferences.isBlank()) {
            return 0;
        }

        String dietary = dietaryPreferences.toLowerCase().trim();

        // ---- Vegetarian case ----
        boolean isVegetarian = dietary.contains("chay");

        if (isVegetarian) {
            int vegetarianCount = 0;
            int nonVegetarianCount = 0;

            for (RestaurantFood rf : restaurantFoods) {
                Food food = foodMap.get(rf.getFoodId());
                if (food != null) {
                    if (isVegetarianFood(food)) {
                        vegetarianCount++;
                    } else {
                        nonVegetarianCount++;
                    }
                }
            }

            int total = vegetarianCount + nonVegetarianCount;
            if (total == 0) return 5; // không có data

            double vegRatio = (double) vegetarianCount / total;

            if (vegetarianCount == 0) {
                return -20; // completely non-vegetarian
            } else if (vegRatio >= 0.5) {
                return 20; // majority vegetarian
            } else if (vegRatio >= 0.3) {
                return 10; // some vegetarian options
            } else if (vegRatio >= 0.1) {
                return 5; // a few vegetarian options
            } else {
                return -5; // very few vegetarian options
            }
        }

        // ---- Non-vegetarian dietary preferences ----
        // (low fat, low calorie, high protein, etc.) → dùng đơn giản hóa
        double dietScore = 0;
        int matchCount = 0;

        for (RestaurantFood rf : restaurantFoods) {
            Food food = foodMap.get(rf.getFoodId());
            if (food != null && dietaryMatches(food, dietary)) {
                matchCount++;
            }
        }

        double ratio = (double) matchCount / restaurantFoods.size();
        if (ratio >= 0.5) dietScore = 15;
        else if (ratio >= 0.3) dietScore = 10;
        else if (ratio >= 0.1) dietScore = 5;
        else dietScore = 0;

        return dietScore;
    }

    /**
     * Kiểm tra món ăn có phải vegetarian không dựa trên tên + description + cuisine_type.
     */
    private boolean isVegetarianFood(Food food) {
        if (food == null) return false;

        String name = food.getName() != null ? food.getName().toLowerCase() : "";
        String cuisineType = food.getCuisineType() != null ? food.getCuisineType().toLowerCase() : "";
        String description = food.getDescription() != null ? food.getDescription().toLowerCase() : "";

        // Nếu chứa từ "chay" → vegetarian
        if (containsKeyword(name, "chay") || containsKeyword(description, "chay")) {
            return true;
        }

        // Nếu cuisine type là Indian vegetarian, vegan → vegetarian
        if (cuisineType.contains("chay") || cuisineType.contains("vegetarian") || cuisineType.contains("vegan")) {
            return true;
        }

        // Kiểm tra các từ khóa không chay
        for (String keyword : NON_VEGETARIAN_KEYWORDS) {
            if (keyword.equals("chay")) continue; // đã xử lý ở trên
            if (containsKeyword(name, keyword) || containsKeyword(description, keyword)) {
                return false; // có chất không chay → không phải vegetarian
            }
        }

        // Mặc định: không biết → không phải vegetarian (cautious)
        return false;
    }

    /**
     * Kiểm tra dietary đơn giản - có matches với đặc điểm món không.
     * (Dùng cho non-vegetarian cases)
     */
    private boolean dietaryMatches(Food food, String dietary) {
        String name = food.getName() != null ? food.getName().toLowerCase() : "";
        String cuisineType = food.getCuisineType() != null ? food.getCuisineType().toLowerCase() : "";
        String description = food.getDescription() != null ? food.getDescription().toLowerCase() : "";

        // Low fat
        if (containsAny(dietary, "ít béo", "low fat", "ít dầu")) {
            // Simple heuristic: nếu tên có "gà", "salad", "rau" → low fat
            if (containsAny(name, "gà", "salad", "rau", "trứng") ||
                containsAny(cuisineType, "giò", "sandwich")) {
                return true;
            }
        }

        // Low calorie / giảm cân
        if (containsAny(dietary, "ít calo", "low calorie", "giảm cân")) {
            if (containsAny(name, "rau", "salad", "gà", " đặc biệt") ||
                containsAny(cuisineType, "salad", "light")) {
                return true;
            }
        }

        // High protein
        if (containsAny(dietary, "nhiều protein", "high protein", "giàu protein")) {
            if (containsAny(name, "thịt", "gà", "sữa", "trứng", "tôm") ||
                containsAny(cuisineType, "đậm đà")) {
                return true;
            }
        }

        return false;
    }

    // ----- BUDGET (max 15) -----

    private double calculateBudgetScore(List<RestaurantFood> restaurantFoods, BigDecimal budget) {
        if (budget == null || budget.compareTo(BigDecimal.ZERO) <= 0) return 0;

        double userBudget = budget.doubleValue();
        int affordableCount = 0;

        for (RestaurantFood rf : restaurantFoods) {
            if (rf.getPrice() != null && rf.getPrice() <= userBudget) {
                affordableCount++;
            }
        }

        if (restaurantFoods.isEmpty()) return 0;

        double ratio = (double) affordableCount / restaurantFoods.size();
        if (ratio >= 0.8) return 15;
        if (ratio >= 0.5) return 12;
        if (ratio >= 0.3) return 8;
        if (ratio >= 0.1) return 4;
        return 0;
    }

    // ----- VARIETY (max 10) -----

    private int calculateVarietyScore(List<RestaurantFood> restaurantFoods) {
        if (restaurantFoods == null) return 0;
        int count = restaurantFoods.size();
        if (count >= 20) return 10;
        if (count >= 15) return 8;
        if (count >= 10) return 6;
        if (count >= 5) return 4;
        return Math.max(1, count);
    }

    // ----- HELPER -----

    private boolean containsKeyword(String text, String keyword) {
        if (text == null || keyword == null || keyword.isBlank()) return false;
        return text.toLowerCase().contains(keyword.toLowerCase());
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null || text.isBlank()) return false;
        for (String keyword : keywords) {
            if (containsKeyword(text, keyword)) return true;
        }
        return false;
    }
}

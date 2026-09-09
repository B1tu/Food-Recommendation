package com.foodrecommendation.recommendation;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.foodrecommendation.entity.Food;
import com.foodrecommendation.entity.Nutrition;
import com.foodrecommendation.entity.UserProfile;

@Component
public class RecommendationEngine {

    public RecommendationResult calculateScore(
            Food food,
            Nutrition nutrition,
            UserProfile profile,
            Map<Integer, Integer> historyFoodCounts) {

        double score = 0.0;

        StringBuilder reason = new StringBuilder();

        // =====================================================
        // 1. PREFERENCE - tối đa 30 điểm
        // =====================================================

        double preferenceScore = calculatePreferenceScore(
                food,
                profile.getPreferences()
        );

        score += preferenceScore;

        if (preferenceScore >= 25) {
            reason.append("Rất phù hợp với sở thích của bạn. ");
        } else if (preferenceScore > 0) {
            reason.append("Phù hợp với sở thích của bạn. ");
        }

        // =====================================================
        // 2. DIETARY - tối đa 20 điểm
        // =====================================================

        double dietaryScore = calculateDietaryScore(
                food,
                nutrition,
                profile.getDietaryPreferences()
        );

        score += dietaryScore;

        if (dietaryScore >= 15) {
            reason.append("Phù hợp tốt với chế độ ăn. ");
        } else if (dietaryScore > 0) {
            reason.append("Có một số điểm phù hợp với chế độ ăn. ");
        }

        // =====================================================
        // 3. NUTRITION - tối đa 20 điểm
        // =====================================================

        double nutritionScore = calculateNutritionScore(
                nutrition,
                profile.getDietaryPreferences()
        );

        score += nutritionScore;

        if (nutritionScore >= 15) {
            reason.append("Thông số dinh dưỡng phù hợp. ");
        } else if (nutritionScore > 0) {
            reason.append("Dinh dưỡng tương đối phù hợp. ");
        }

        // =====================================================
        // 4. BUDGET - tối đa 20 điểm
        // =====================================================

        double budgetScore = calculateBudgetScore(
                food,
                profile.getBudget()
        );

        score += budgetScore;

        if (budgetScore >= 18) {
            reason.append("Giá rất phù hợp ngân sách. ");
        } else if (budgetScore >= 10) {
            reason.append("Giá phù hợp ngân sách. ");
        } else if (budgetScore > 0) {
            reason.append("Giá hơi cao so với ngân sách. ");
        }

        // =====================================================
        // 5. HISTORY - tối đa 10 điểm
        // =====================================================

        double historyScore = calculateHistoryScore(
                food,
                historyFoodCounts
        );

        score += historyScore;

        if (historyScore >= 10) {
            reason.append("Bạn đã từng chọn món này. ");
        } else if (historyScore > 0) {
            reason.append("Có điểm tương đồng với lựa chọn trước đây. ");
        }

        // =====================================================
        // GIỚI HẠN 0 - 100
        // =====================================================

        score = Math.min(score, 100.0);

        return new RecommendationResult(
                food.getFoodId(),
                score,
                reason.toString().trim()
        );
    }


    // =========================================================
    // 1. PREFERENCE
    // =========================================================

    private double calculatePreferenceScore(
            Food food,
            String preferences) {

        if (preferences == null || preferences.isBlank()) {
            return 0;
        }

        String pref = preferences.toLowerCase().trim();

        double score = 0;

        /*
         * Tên món
         * Ví dụ:
         * preferences = "phở"
         * food.name = "Phở bò"
         */
        if (containsKeyword(food.getName(), pref)) {
            score += 20;
        }

        /*
         * Loại ẩm thực
         * Ví dụ:
         * preferences = "việt"
         * cuisineType = "Vietnamese"
         */
        if (containsKeyword(food.getCuisineType(), pref)) {
            score += 10;
        }

        /*
         * Description
         * Giúp recommendation hiểu thêm
         * thông tin mô tả món ăn.
         */
        if (containsKeyword(food.getDescription(), pref)) {
            score += 5;
        }

        return Math.min(score, 30);
    }


    // =========================================================
    // 2. DIETARY PREFERENCE
    // =========================================================

    private double calculateDietaryScore(
            Food food,
            Nutrition nutrition,
            String dietaryPreferences) {

        if (dietaryPreferences == null
                || dietaryPreferences.isBlank()
                || nutrition == null) {

            return 0;
        }

        String dietary =
                dietaryPreferences.toLowerCase().trim();

        double score = 0;

        // -----------------------------------------------------
        // LOW FAT
        // -----------------------------------------------------

        if (containsAny(
                dietary,
                "ít béo",
                "low fat",
                "ít dầu")) {

            if (nutrition.getFat() != null) {

                double fat = nutrition.getFat();

                if (fat <= 10) {
                    score += 20;
                } else if (fat <= 15) {
                    score += 15;
                } else if (fat <= 20) {
                    score += 8;
                }
            }
        }

        // -----------------------------------------------------
        // LOW CALORIE / GIẢM CÂN
        // -----------------------------------------------------

        if (containsAny(
                dietary,
                "ít calo",
                "low calorie",
                "giảm cân")) {

            if (nutrition.getCalories() != null) {

                double calories = nutrition.getCalories();

                if (calories <= 400) {
                    score += 20;
                } else if (calories <= 500) {
                    score += 15;
                } else if (calories <= 600) {
                    score += 8;
                }
            }
        }

        // -----------------------------------------------------
        // HIGH PROTEIN
        // -----------------------------------------------------

        if (containsAny(
                dietary,
                "nhiều protein",
                "high protein",
                "giàu protein")) {

            if (nutrition.getProtein() != null) {

                double protein = nutrition.getProtein();

                if (protein >= 30) {
                    score += 20;
                } else if (protein >= 20) {
                    score += 15;
                } else if (protein >= 15) {
                    score += 8;
                }
            }
        }

        return Math.min(score, 20);
    }


    // =========================================================
    // 3. BUDGET
    // =========================================================

    private double calculateBudgetScore(
            Food food,
            BigDecimal budget) {

        if (food.getPrice() == null
                || budget == null) {

            return 0;
        }

        double price = food.getPrice();
        double userBudget = budget.doubleValue();

        if (userBudget <= 0) {
            return 0;
        }

        /*
         * Giá nằm trong ngân sách.
         */

        if (price <= userBudget) {

            double ratio =
                    price / userBudget;

            /*
             * 80% - 100% ngân sách
             * → rất phù hợp
             */
            if (ratio >= 0.80) {
                return 20;
            }

            /*
             * 60% - 80%
             */
            if (ratio >= 0.60) {
                return 18;
            }

            /*
             * 40% - 60%
             */
            if (ratio >= 0.40) {
                return 15;
            }

            /*
             * Dưới 40%
             */
            return 10;
        }

        /*
         * Cho phép món vượt ngân sách một chút
         * nhưng điểm thấp.
         */

        double overRatio =
                (price - userBudget) / userBudget;

        if (overRatio <= 0.10) {
            return 5;
        }

        return 0;
    }


    // =========================================================
    // 4. NUTRITION
    // =========================================================

    private double calculateNutritionScore(
            Nutrition nutrition,
            String dietaryPreferences) {

        if (nutrition == null) {
            return 0;
        }

        /*
         * Nếu user không yêu cầu dinh dưỡng,
         * không để nutrition chi phối recommendation.
         */

        if (dietaryPreferences == null
                || dietaryPreferences.isBlank()) {

            return 0;
        }

        String dietary =
                dietaryPreferences.toLowerCase();

        double score = 0;

        // -----------------------------------------------------
        // LOW CALORIE
        // -----------------------------------------------------

        if (containsAny(
                dietary,
                "ít calo",
                "low calorie",
                "giảm cân")) {

            if (nutrition.getCalories() != null) {

                double calories =
                        nutrition.getCalories();

                if (calories <= 400) {
                    score += 10;
                } else if (calories <= 500) {
                    score += 7;
                } else if (calories <= 600) {
                    score += 3;
                }
            }
        }

        // -----------------------------------------------------
        // HIGH PROTEIN
        // -----------------------------------------------------

        if (containsAny(
                dietary,
                "nhiều protein",
                "high protein",
                "giàu protein")) {

            if (nutrition.getProtein() != null) {

                double protein =
                        nutrition.getProtein();

                if (protein >= 30) {
                    score += 10;
                } else if (protein >= 20) {
                    score += 7;
                } else if (protein >= 15) {
                    score += 3;
                }
            }
        }

        // -----------------------------------------------------
        // LOW FAT
        // -----------------------------------------------------

        if (containsAny(
                dietary,
                "ít béo",
                "low fat",
                "ít dầu")) {

            if (nutrition.getFat() != null) {

                double fat =
                        nutrition.getFat();

                if (fat <= 10) {
                    score += 8;
                } else if (fat <= 15) {
                    score += 5;
                } else if (fat <= 20) {
                    score += 2;
                }
            }
        }

        /*
         * Giới hạn tổng Nutrition = 20
         */
        return Math.min(score, 20);
    }


    // =========================================================
    // 5. HISTORY
    // =========================================================

    private double calculateHistoryScore(
            Food food,
            Map<Integer, Integer> historyFoodCounts
    ) {

        if (historyFoodCounts == null
                || historyFoodCounts.isEmpty()
                || food.getFoodId() == null) {

            return 0;
        }

        Integer count =
                historyFoodCounts.get(food.getFoodId());

        if (count == null || count <= 0) {
            return 0;
        }

        /*
        * V3:
        *
        * 1 lần  -> 4 điểm
        * 2 lần  -> 6 điểm
        * 3 lần  -> 8 điểm
        * 4+ lần -> 10 điểm
        */

        if (count >= 4) {
            return 10;
        }

        if (count == 3) {
            return 8;
        }

        if (count == 2) {
            return 6;
        }

        return 4;
    }


    // =========================================================
    // HELPER
    // =========================================================

    private boolean containsKeyword(
            String text,
            String keyword) {

        if (text == null
                || keyword == null
                || keyword.isBlank()) {

            return false;
        }

        return text.toLowerCase().contains(
                keyword.toLowerCase()
        );
    }


    private boolean containsAny(
            String text,
            String... keywords) {

        if (text == null
                || text.isBlank()) {

            return false;
        }

        String lowerText =
                text.toLowerCase();

        for (String keyword : keywords) {

            if (lowerText.contains(
                    keyword.toLowerCase())) {

                return true;
            }
        }

        return false;
    }
}
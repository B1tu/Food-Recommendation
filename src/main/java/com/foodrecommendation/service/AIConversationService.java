package com.foodrecommendation.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.foodrecommendation.dto.NearbyRestaurantDto;
import com.foodrecommendation.dto.RecommendedFoodDto;
import com.foodrecommendation.entity.AIConversation;
import com.foodrecommendation.entity.Food;
import com.foodrecommendation.entity.History;
import com.foodrecommendation.entity.Nutrition;
import com.foodrecommendation.entity.Restaurant;
import com.foodrecommendation.entity.RestaurantFood;
import com.foodrecommendation.entity.UserProfile;
import com.foodrecommendation.recommendation.RecommendationEngine;
import com.foodrecommendation.recommendation.RecommendationResult;
import com.foodrecommendation.repository.AIConversationRepository;
import com.foodrecommendation.repository.FoodRepository;
import com.foodrecommendation.repository.NutritionRepository;
import com.foodrecommendation.repository.RestaurantFoodRepository;
import com.foodrecommendation.repository.RestaurantRepository;

@Service
public class AIConversationService {

    // Ràng buộc để giữ prompt gọn, tránh chi phí/độ trễ tăng vô hạn khi
    // dữ liệu món ăn ngày càng nhiều (hiện có 45 món, sẽ còn tăng thêm).
    private static final int MAX_FOODS_IN_CONTEXT = 12;
    private static final int MAX_RESTAURANTS_IN_CONTEXT = 5;
    private static final int MAX_HISTORY_TURNS = 5;
    private static final int MAX_USER_MESSAGE_LENGTH = 1000;

    private final AIConversationRepository aiConversationRepository;
    private final FoodRepository foodRepository;
    private final RestaurantRepository restaurantRepository;
    private final NutritionRepository nutritionRepository;
    private final RestaurantFoodRepository restaurantFoodRepository;
    private final UserProfileService userProfileService;
    private final HistoryService historyService;
    private final RecommendationEngine recommendationEngine;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String geminiApiKey;
    private final String geminiBaseUrl;

    public AIConversationService(
            AIConversationRepository aiConversationRepository,
            FoodRepository foodRepository,
            RestaurantRepository restaurantRepository,
            NutritionRepository nutritionRepository,
            RestaurantFoodRepository restaurantFoodRepository,
            UserProfileService userProfileService,
            HistoryService historyService,
            RecommendationEngine recommendationEngine,
            @Value("${gemini.api.key}") String geminiApiKey,
            @Value("${gemini.base.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent}") String geminiBaseUrl) {
        this.aiConversationRepository = aiConversationRepository;
        this.foodRepository = foodRepository;
        this.restaurantRepository = restaurantRepository;
        this.nutritionRepository = nutritionRepository;
        this.restaurantFoodRepository = restaurantFoodRepository;
        this.userProfileService = userProfileService;
        this.historyService = historyService;
        this.recommendationEngine = recommendationEngine;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.geminiApiKey = geminiApiKey;
        this.geminiBaseUrl = geminiBaseUrl;
    }

    public List<AIConversation> getAllConversations() {
        return aiConversationRepository.findAll();
    }

    public AIConversation getConversationById(Integer id) {
        return aiConversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
    }

    public List<AIConversation> getConversationsByUser(Integer userId) {
        return aiConversationRepository.findByUserId(userId);
    }

    public AIConversation createConversation(AIConversation conversation) {
        if (conversation.getCreatedAt() == null) {
            conversation.setCreatedAt(LocalDateTime.now());
        }

        String userMessage = conversation.getUserMessage();

        // ===== Ràng buộc đầu vào =====
        if (userMessage == null || userMessage.isBlank()) {
            throw new IllegalArgumentException("Nội dung câu hỏi không được để trống");
        }
        if (userMessage.length() > MAX_USER_MESSAGE_LENGTH) {
            userMessage = userMessage.substring(0, MAX_USER_MESSAGE_LENGTH);
        }

        Map<Integer, Food> foodContextIndex = new HashMap<>();
        Map<Integer, Restaurant> restaurantContextIndex = new HashMap<>();
        String foodRestaurantContext = buildFoodRestaurantContext(
                conversation.getUserId(), foodContextIndex, restaurantContextIndex);
        List<NearbyRestaurantDto> nearbyRestaurants = findNearbyRestaurants(
                conversation.getLatitude(), conversation.getLongitude(), userMessage);
        boolean hasLocation = conversation.getLatitude() != null && conversation.getLongitude() != null;
        String locationContext = buildLocationContext(nearbyRestaurants, hasLocation);
        String historyContext = buildConversationHistoryContext(conversation.getSessionId());
        GeminiStructuredResult result = callGeminiAPI(userMessage, foodRestaurantContext, locationContext, historyContext);

        conversation.setUserMessage(userMessage);
        conversation.setAiResponse(result.replyText);
        conversation.setNearbyRestaurants(nearbyRestaurants);

        // Card món ăn chỉ dùng để LẤP chỗ trống khi nhánh quận/vị trí
        // (nearbyRestaurants) rỗng — không trộn 2 nguồn card cùng lúc.
        if (nearbyRestaurants == null || nearbyRestaurants.isEmpty()) {
            List<RecommendedFoodDto> recommendedFoods = buildRecommendedFoods(
                    result.recommendedFoodIds, foodContextIndex);
            conversation.setRecommendedFoods(recommendedFoods);
        } else {
            conversation.setRecommendedFoods(Collections.emptyList());
        }

        return aiConversationRepository.save(conversation);
    }

    // Đối chiếu các foodId Gemini chọn (trong JSON có cấu trúc trả về) với
    // đúng những món đã thực sự đưa vào context — bỏ qua ID lạ/không có
    // thật, tuyệt đối không tự dựng dữ liệu theo tên AI viết ra.
    private List<RecommendedFoodDto> buildRecommendedFoods(List<Integer> foodIds, Map<Integer, Food> foodContextIndex) {
        List<RecommendedFoodDto> result = new ArrayList<>();
        if (foodIds == null || foodIds.isEmpty()) return result;
        for (Integer foodId : foodIds) {
            Food food = foodContextIndex.get(foodId);
            if (food == null) continue; // ID không có trong danh sách đã đưa cho Gemini -> bỏ qua
            RestaurantFood rf = findAvailableRestaurantFood(food.getFoodId());
            Double price = rf != null && rf.getPrice() != null ? rf.getPrice() : food.getPrice();
            Restaurant restaurant = rf != null ? restaurantRepository.findById(rf.getRestaurantId()).orElse(null) : null;
            result.add(new RecommendedFoodDto(
                    food.getFoodId(),
                    food.getName(),
                    price,
                    restaurant != null ? restaurant.getName() : null,
                    restaurant != null ? restaurant.getAddress() : null,
                    restaurant != null ? restaurant.getOpeningHours() : null
            ));
            if (result.size() >= MAX_RESTAURANTS_IN_CONTEXT) break;
        }
        return result;
    }

    // =========================================================
    // Ngữ cảnh món ăn / nhà hàng - có dùng RecommendationEngine
    // =========================================================

    private String buildFoodRestaurantContext(
            Integer userId, Map<Integer, Food> foodContextIndex, Map<Integer, Restaurant> restaurantContextIndex) {
        List<Food> allFoods = foodRepository.findAll();
        List<Food> selectedFoods;

        UserProfile profile = userId != null ? userProfileService.getProfileByUserId(userId) : null;
        boolean hasPersonalizationSignal = profile != null && (
                notBlank(profile.getPreferences())
                        || notBlank(profile.getDietaryPreferences())
                        || (profile.getBudget() != null && profile.getBudget().doubleValue() > 0)
        );

        Map<Integer, String> foodReasons = new HashMap<>();

        if (hasPersonalizationSignal) {
            // Có hồ sơ + ít nhất 1 tín hiệu cá nhân hóa -> chấm điểm bằng
            // RecommendationEngine, chọn ra top món điểm cao nhất.
            Map<Integer, Integer> historyFoodCounts = buildHistoryFoodCounts(userId);

            List<ScoredFood> scored = new ArrayList<>();
            for (Food food : allFoods) {
                Nutrition nutrition = nutritionRepository.findByFoodId(food.getFoodId()).orElse(null);
                RecommendationResult result = recommendationEngine.calculateScore(
                        food, nutrition, profile, historyFoodCounts);
                scored.add(new ScoredFood(food, result));
            }
            scored.sort(Comparator.comparingDouble((ScoredFood sf) -> sf.result.getScore()).reversed());

            selectedFoods = new ArrayList<>();
            for (ScoredFood sf : scored) {
                if (selectedFoods.size() >= MAX_FOODS_IN_CONTEXT) break;
                // Chỉ đưa vào context các món thật sự có điểm phù hợp nào đó,
                // tránh nhét món hoàn toàn không liên quan chỉ để lấp đủ số lượng.
                if (sf.result.getScore() <= 0 && selectedFoods.size() >= 5) continue;
                selectedFoods.add(sf.food);
                if (notBlank(sf.result.getReason())) {
                    foodReasons.put(sf.food.getFoodId(), sf.result.getReason());
                }
            }
            if (selectedFoods.isEmpty()) {
                selectedFoods = fallbackFoods(allFoods);
            }
        } else {
            // Chưa có hồ sơ / chưa có tín hiệu cá nhân hóa nào -> không có gì
            // để chấm điểm, dùng danh sách mặc định thay vì dump hết 45 món.
            selectedFoods = fallbackFoods(allFoods);
        }

        StringBuilder context = new StringBuilder();

        if (!selectedFoods.isEmpty()) {
            context.append("DANH SÁCH MÓN ĂN GỢI Ý (đã chọn lọc phù hợp với người dùng):\n");
            for (Food food : selectedFoods) {
                foodContextIndex.put(food.getFoodId(), food);
                RestaurantFood rf = findAvailableRestaurantFood(food.getFoodId());
                Double price = rf != null && rf.getPrice() != null ? rf.getPrice() : food.getPrice();
                Restaurant restaurant = rf != null ? restaurantRepository.findById(rf.getRestaurantId()).orElse(null) : null;

                context.append(String.format("- [F%d] %s | giá: %.0f VND | loại: %s",
                        food.getFoodId(),
                        food.getName(),
                        price != null ? price : 0,
                        food.getCuisineType() != null ? food.getCuisineType() : "không rõ"));

                if (restaurant != null) {
                    context.append(String.format(" | nhà hàng: %s | địa chỉ: %s | giờ mở: %s",
                            restaurant.getName(),
                            restaurant.getAddress() != null ? restaurant.getAddress() : "chưa có",
                            restaurant.getOpeningHours() != null ? restaurant.getOpeningHours() : "chưa có"));
                }

                String reason = foodReasons.get(food.getFoodId());
                if (notBlank(reason)) {
                    context.append(" | lý do phù hợp: ").append(reason);
                }
                context.append("\n");
            }
            context.append("\n");
        }

        List<Restaurant> topRestaurants = restaurantRepository.findTop5ByOrderByRatingDesc();
        if (topRestaurants != null && !topRestaurants.isEmpty()) {
            context.append("DANH SÁCH NHÀ HÀNG NỔI BẬT (rating cao):\n");
            int count = 0;
            for (Restaurant r : topRestaurants) {
                if (count >= MAX_RESTAURANTS_IN_CONTEXT) break;
                restaurantContextIndex.put(r.getRestaurantId(), r);
                context.append(String.format("- [R%d] %s | rating: %.1f sao | địa chỉ: %s | giờ mở: %s\n",
                        r.getRestaurantId(),
                        r.getName(),
                        r.getRating() != null ? r.getRating() : 0.0,
                        r.getAddress() != null ? r.getAddress() : "chưa có",
                        r.getOpeningHours() != null ? r.getOpeningHours() : "chưa có"));
                count++;
            }
            context.append("\n");
        }

        return context.toString();
    }

    private List<Food> fallbackFoods(List<Food> allFoods) {
        List<Food> result = new ArrayList<>(allFoods);
        result.sort(Comparator.comparing(Food::getFoodId, Comparator.nullsLast(Comparator.naturalOrder())));
        if (result.size() > MAX_FOODS_IN_CONTEXT) {
            return result.subList(0, MAX_FOODS_IN_CONTEXT);
        }
        return result;
    }

    private Map<Integer, Integer> buildHistoryFoodCounts(Integer userId) {
        Map<Integer, Integer> counts = new HashMap<>();
        List<History> historyList = historyService.getByUserId(userId);
        if (historyList == null) return counts;
        for (History h : historyList) {
            if (h.getFoodId() == null) continue;
            counts.merge(h.getFoodId(), 1, Integer::sum);
        }
        return counts;
    }

    private RestaurantFood findAvailableRestaurantFood(Integer foodId) {
        List<RestaurantFood> options = restaurantFoodRepository.findByFoodId(foodId);
        if (options == null || options.isEmpty()) return null;
        for (RestaurantFood rf : options) {
            if (Boolean.TRUE.equals(rf.getIsAvailable())) {
                return rf;
            }
        }
        return options.get(0);
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static class ScoredFood {
        final Food food;
        final RecommendationResult result;

        ScoredFood(Food food, RecommendationResult result) {
            this.food = food;
            this.result = result;
        }
    }

    // =========================================================
    // Ngữ cảnh nhà hàng gần vị trí người dùng (nếu trình duyệt gửi tọa độ)
    // =========================================================

    private static final double NEARBY_RADIUS_KM = 20.0;

    // Các từ quá chung chung, không dùng để lọc món 1 mình (vd chỉ nói "cơm"
    // thì không rõ là cơm tấm, cơm chay hay cơm cà ri).
    private static final List<String> GENERIC_DISH_WORDS = Arrays.asList(
            "cơm", "món", "ăn", "quán", "đồ", "bánh", "nước", "chả", "sốt", "thịt", "canh"
    );

    private List<NearbyRestaurantDto> findNearbyRestaurants(Double lat, Double lng, String userMessage) {
        List<Food> allFoods = foodRepository.findAll();
        String dishKeyword = extractDishKeyword(userMessage, allFoods);
        List<String> districtKeywords = extractDistrictKeywords(userMessage);
        boolean hasDistrict = !districtKeywords.isEmpty();
        boolean hasLocation = lat != null && lng != null;

        List<Restaurant> candidates;
        if (hasDistrict) {
            // Người dùng nêu rõ (một hoặc nhiều) quận/khu vực (vd "quận 1 hoặc
            // quận 3") -> lấy nhà hàng khớp BẤT KỲ quận nào trong danh sách,
            // KHÔNG giới hạn theo khoảng cách GPS (họ hỏi theo địa danh cụ thể,
            // không phải "gần tôi" — kể cả khi chưa cấp quyền định vị).
            candidates = new ArrayList<>();
            for (Restaurant r : restaurantRepository.findAll()) {
                if (r.getAddress() != null && addressMatchesAnyDistrict(r.getAddress(), districtKeywords)) {
                    candidates.add(r);
                }
            }
        } else if (hasLocation) {
            candidates = restaurantRepository.findByDistance(lat, lng, NEARBY_RADIUS_KM);
        } else {
            return Collections.emptyList();
        }

        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }

        List<NearbyRestaurantDto> result = new ArrayList<>();
        for (Restaurant r : candidates) {
            Double distanceKm = null;
            if (hasLocation && r.getLatitude() != null && r.getLongitude() != null) {
                distanceKm = haversineKm(lat, lng, r.getLatitude(), r.getLongitude());
                // Chỉ áp dụng giới hạn bán kính khi KHÔNG có tên quận cụ thể
                // (đã lọc theo địa chỉ rồi thì không cần giới hạn khoảng cách nữa).
                if (!hasDistrict && distanceKm > NEARBY_RADIUS_KM) continue;
            }

            // Người dùng hỏi 1 món cụ thể (vd "cơm tấm gần đây") -> chỉ giữ
            // nhà hàng thật sự có bán món đó, tránh trả về lạc đề như trước.
            if (dishKeyword != null && !restaurantServesDish(r.getRestaurantId(), dishKeyword)) {
                continue;
            }

            result.add(new NearbyRestaurantDto(
                    r.getRestaurantId(),
                    r.getName(),
                    r.getRating(),
                    distanceKm != null ? Math.round(distanceKm * 10.0) / 10.0 : null,
                    r.getAddress(),
                    r.getOpeningHours()
            ));
        }

        if (hasLocation) {
            result.sort(Comparator.comparing(
                    NearbyRestaurantDto::getDistanceKm,
                    Comparator.nullsLast(Comparator.naturalOrder())));
        } else {
            result.sort(Comparator.comparing(
                    (NearbyRestaurantDto dto) -> dto.getRating() != null ? dto.getRating() : 0.0)
                    .reversed());
        }
        if (result.size() > MAX_RESTAURANTS_IN_CONTEXT) {
            return result.subList(0, MAX_RESTAURANTS_IN_CONTEXT);
        }
        return result;
    }

    // Tìm TẤT CẢ cụm từ chỉ quận/khu vực trong câu hỏi (vd "quận 1 hoặc quận
    // 3" -> ["quận 1", "quận 3"]), không chỉ quận đầu tiên như trước.
    private List<String> extractDistrictKeywords(String userMessage) {
        List<String> found = new ArrayList<>();
        if (userMessage == null) return found;
        String msg = userMessage.toLowerCase();

        java.util.regex.Matcher m = java.util.regex.Pattern.compile("quận\\s*(\\d{1,2})\\b").matcher(msg);
        while (m.find()) {
            String d = "quận " + m.group(1);
            if (!found.contains(d)) found.add(d);
        }

        String[] namedDistricts = {
                "phú nhuận", "bình thạnh", "tân bình", "tân phú", "gò vấp",
                "thủ đức", "bình tân", "nhà bè", "hóc môn", "củ chi", "bình chánh", "cần giờ"
        };
        for (String d : namedDistricts) {
            if (msg.contains(d) && !found.contains(d)) found.add(d);
        }
        return found;
    }

    // So khớp địa chỉ nhà hàng với BẤT KỲ quận/khu vực nào trong danh sách
    // được hỏi. Với quận đánh số, tránh "quận 1" khớp nhầm vào "quận
    // 10"/"quận 11"/"quận 12".
    private boolean addressMatchesAnyDistrict(String address, List<String> districtKeywords) {
        String addr = address.toLowerCase();
        for (String districtKeyword : districtKeywords) {
            java.util.regex.Matcher numMatch = java.util.regex.Pattern.compile("^quận\\s*(\\d{1,2})$").matcher(districtKeyword);
            boolean matched;
            if (numMatch.matches()) {
                String num = numMatch.group(1);
                matched = java.util.regex.Pattern.compile("quận\\s*" + num + "(?=[,.]|$)").matcher(addr).find();
            } else {
                matched = addr.contains(districtKeyword);
            }
            if (matched) return true;
        }
        return false;
    }

    // Công thức Haversine — tính khoảng cách thực tế (km) giữa 2 tọa độ,
    // chính xác hơn phép lọc hình chữ nhật gần đúng của findByDistance.
    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371.0; // bán kính Trái Đất (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // Tìm cụm từ chỉ tên món trong câu hỏi của người dùng, dựa trên tên các
    // món có trong DB (vd câu hỏi "cơm tấm gần đây" -> khớp "cơm tấm" từ
    // tên món "Cơm Tấm Sườn Bì Chả"). Đây là heuristic đơn giản (so khớp
    // chuỗi con), không phải NLU thật sự — đủ dùng cho quy mô đồ án.
    private String extractDishKeyword(String userMessage, List<Food> allFoods) {
        if (userMessage == null || userMessage.isBlank() || allFoods == null) {
            return null;
        }
        String msg = " " + userMessage.toLowerCase() + " ";

        // Ưu tiên khớp cụm 2 từ đầu tên món (đặc trưng hơn 1 từ đơn lẻ).
        for (Food f : allFoods) {
            if (f.getName() == null) continue;
            String[] words = f.getName().toLowerCase().trim().split("\\s+");
            if (words.length >= 2) {
                String prefix2 = words[0] + " " + words[1];
                if (msg.contains(prefix2)) {
                    return prefix2;
                }
            }
        }

        // Fallback: khớp 1 từ đầu tiên nếu đủ đặc trưng (loại các từ quá chung chung).
        for (Food f : allFoods) {
            if (f.getName() == null) continue;
            String first = f.getName().toLowerCase().trim().split("\\s+")[0];
            if (!GENERIC_DISH_WORDS.contains(first) && msg.contains(" " + first + " ")) {
                return first;
            }
        }

        return null;
    }

    // Kiểm tra nhà hàng có món nào chứa từ khóa món ăn không (dựa trên bảng
    // RESTAURANT_FOOD, không phải toàn bộ danh sách món của cả hệ thống).
    private boolean restaurantServesDish(Integer restaurantId, String dishKeyword) {
        List<RestaurantFood> items = restaurantFoodRepository.findByRestaurantId(restaurantId);
        if (items == null || items.isEmpty()) {
            return false;
        }
        for (RestaurantFood rf : items) {
            Food f = foodRepository.findById(rf.getFoodId()).orElse(null);
            if (f != null && f.getName() != null && f.getName().toLowerCase().contains(dishKeyword)) {
                return true;
            }
        }
        return false;
    }

    private String buildLocationContext(List<NearbyRestaurantDto> nearbyRestaurants, boolean hasLocation) {
        if (nearbyRestaurants == null || nearbyRestaurants.isEmpty()) {
            if (hasLocation) {
                // Có tọa độ thật nhưng không quán nào phù hợp (vd hỏi 1 món cụ
                // thể mà không quán gần đó có bán) — khác với "chưa có vị trí".
                return "VỊ TRÍ NGƯỜI DÙNG: đã nhận được tọa độ, nhưng không tìm thấy quán nào phù hợp " +
                        "với yêu cầu trong bán kính " + (int) NEARBY_RADIUS_KM + "km. " +
                        "Hãy nói thật là chưa tìm thấy quán phù hợp gần đó, đừng bịa ra quán.\n\n";
            }
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("NHÀ HÀNG PHÙ HỢP VỚI YÊU CẦU VỀ VỊ TRÍ:\n");
        for (NearbyRestaurantDto r : nearbyRestaurants) {
            String distancePart = r.getDistanceKm() != null
                    ? String.format("cách %.1f km", r.getDistanceKm())
                    : "khoảng cách chưa xác định";
            sb.append(String.format("- %s | %s | rating: %.1f sao | địa chỉ: %s | giờ mở: %s\n",
                    r.getName(),
                    distancePart,
                    r.getRating() != null ? r.getRating() : 0.0,
                    r.getAddress() != null ? r.getAddress() : "chưa có",
                    r.getOpeningHours() != null ? r.getOpeningHours() : "chưa có"));
        }
        sb.append("Lưu ý: các nhà hàng này ĐÃ được hiển thị dưới dạng thẻ riêng cho người dùng rồi, " +
                "KHÔNG cần liệt kê lại chi tiết tên/địa chỉ/rating trong câu trả lời văn bản — " +
                "chỉ cần nói ngắn gọn 1 câu dẫn (vd: đây là vài quán gần bạn) rồi có thể hỏi thêm nhu cầu.\n");
        sb.append("\n");
        return sb.toString();
    }

    // =========================================================
    // Ngữ cảnh lịch sử hội thoại (trong cùng 1 phiên chat)
    // =========================================================

    private String buildConversationHistoryContext(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return "";
        }

        List<AIConversation> recent = aiConversationRepository
                .findTop5BySessionIdOrderByCreatedAtDesc(sessionId);
        if (recent == null || recent.isEmpty()) {
            return "";
        }

        List<AIConversation> chronological = new ArrayList<>(recent);
        Collections.reverse(chronological);

        int fromIndex = Math.max(0, chronological.size() - MAX_HISTORY_TURNS);
        chronological = chronological.subList(fromIndex, chronological.size());

        StringBuilder sb = new StringBuilder();
        sb.append("LỊCH SỬ HỘI THOẠI TRONG PHIÊN NÀY (từ cũ đến mới):\n");
        for (AIConversation turn : chronological) {
            sb.append("Người dùng: ").append(turn.getUserMessage()).append("\n");
            sb.append("FoodAI: ").append(turn.getAiResponse()).append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    // =========================================================
    // Gọi Gemini API
    // =========================================================

    // Schema JSON bắt buộc Gemini phải trả về (structured output) — thay vì
    // để AI tự viết văn bản thường, ép nó trả đúng 3 trường để backend dựng
    // card món ăn/nhà hàng chính xác, không cần đoán từ câu chữ tự do.
    private static final Map<String, Object> GEMINI_RESPONSE_SCHEMA = Map.of(
            "type", "OBJECT",
            "properties", Map.of(
                    "replyText", Map.of("type", "STRING"),
                    "recommendedFoodIds", Map.of("type", "ARRAY", "items", Map.of("type", "INTEGER")),
                    "recommendedRestaurantIds", Map.of("type", "ARRAY", "items", Map.of("type", "INTEGER"))
            ),
            "required", List.of("replyText")
    );

    private GeminiStructuredResult callGeminiAPI(String userMessage, String foodRestaurantContext, String locationContext, String historyContext) {
        try {
            String url = geminiBaseUrl + "?key=" + geminiApiKey;
            String prompt = buildPrompt(userMessage, foodRestaurantContext, locationContext, historyContext);

            Map<String, Object> content = Map.of(
                "parts", new Object[]{ Map.of("text", prompt) }
            );
            Map<String, Object> generationConfig = Map.of(
                "responseMimeType", "application/json",
                "responseSchema", GEMINI_RESPONSE_SCHEMA
            );
            Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{ content },
                "generationConfig", generationConfig
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            String rawText = null;
            if (responseBody != null && responseBody.containsKey("candidates")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> firstCandidate = candidates.get(0);
                    if (firstCandidate.containsKey("content")) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> contentItem = (Map<String, Object>) firstCandidate.get("content");
                        if (contentItem != null && contentItem.containsKey("parts")) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> parts = (List<Map<String, Object>>) contentItem.get("parts");
                            if (parts != null && !parts.isEmpty()) {
                                rawText = (String) parts.get(0).get("text");
                            }
                        }
                    }
                }
            }

            if (rawText == null) {
                return new GeminiStructuredResult("Xin lỗi, tôi không thể trả lời câu hỏi này.",
                        Collections.emptyList(), Collections.emptyList());
            }

            return parseStructuredReply(rawText);

        } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
            // Hết quota Gemini API (thường là gói free 20 request/ngày) — thông báo
            // rõ ràng hơn cho người dùng thay vì lỗi hệ thống chung chung.
            System.err.println("Gemini API rate limit/quota exceeded: " + e.getMessage());
            return new GeminiStructuredResult(
                    "Hiện tại hệ thống đang có quá nhiều yêu cầu (đã đạt giới hạn API), " +
                            "vui lòng thử lại sau ít phút.",
                    Collections.emptyList(), Collections.emptyList());
        } catch (Exception e) {
            System.err.println("Error calling Gemini API: " + e.getMessage());
            e.printStackTrace();
            return new GeminiStructuredResult("Xin lỗi, hệ thống đang gặp sự cố.",
                    Collections.emptyList(), Collections.emptyList());
        }
    }

    // Parse JSON Gemini trả về (đúng theo GEMINI_RESPONSE_SCHEMA). Nếu vì lý
    // do nào đó JSON không hợp lệ hoặc thiếu field (không nên xảy ra vì đã
    // ép responseSchema, nhưng vẫn phòng hờ) -> fallback: coi nguyên văn bản
    // thô là replyText, 2 danh sách ID rỗng — KHÔNG để lỗi parse làm mất
    // phản hồi hay crash request.
    private GeminiStructuredResult parseStructuredReply(String rawText) {
        try {
            JsonNode node = objectMapper.readTree(rawText);
            String replyText = node.hasNonNull("replyText") ? node.get("replyText").asText() : rawText;
            List<Integer> foodIds = new ArrayList<>();
            if (node.has("recommendedFoodIds") && node.get("recommendedFoodIds").isArray()) {
                for (JsonNode idNode : node.get("recommendedFoodIds")) {
                    if (idNode.isInt()) foodIds.add(idNode.asInt());
                }
            }
            List<Integer> restaurantIds = new ArrayList<>();
            if (node.has("recommendedRestaurantIds") && node.get("recommendedRestaurantIds").isArray()) {
                for (JsonNode idNode : node.get("recommendedRestaurantIds")) {
                    if (idNode.isInt()) restaurantIds.add(idNode.asInt());
                }
            }
            return new GeminiStructuredResult(replyText, foodIds, restaurantIds);
        } catch (Exception e) {
            System.err.println("Không parse được JSON Gemini trả về, dùng nguyên văn bản: " + e.getMessage());
            return new GeminiStructuredResult(rawText, Collections.emptyList(), Collections.emptyList());
        }
    }

    private static class GeminiStructuredResult {
        final String replyText;
        final List<Integer> recommendedFoodIds;
        final List<Integer> recommendedRestaurantIds;

        GeminiStructuredResult(String replyText, List<Integer> recommendedFoodIds, List<Integer> recommendedRestaurantIds) {
            this.replyText = replyText;
            this.recommendedFoodIds = recommendedFoodIds;
            this.recommendedRestaurantIds = recommendedRestaurantIds;
        }
    }

    private String buildPrompt(String userMessage, String foodRestaurantContext, String locationContext, String historyContext) {
        String systemInstruction =
            "Bạn là FoodAI – trợ lý ẩm thực thông minh.\n" +
            "Bạn PHẢI trả lời đúng theo schema JSON đã cho (responseSchema), gồm 3 trường: " +
            "replyText, recommendedFoodIds, recommendedRestaurantIds. KHÔNG trả về văn bản tự do " +
            "ngoài JSON này.\n\n" +
            "replyText: MỘT CÂU DẪN NGẮN GỌN (1 câu, không liệt kê chi tiết món/giá/địa chỉ trong " +
            "câu này vì phần chi tiết sẽ hiển thị riêng dưới dạng thẻ/card). Ví dụ: " +
            "\"Dưới đây là vài món phù hợp cho bạn:\" hoặc \"Mình chưa tìm thấy món nào phù hợp trong " +
            "danh sách hiện có, bạn thử hỏi khác xem sao nhé.\" nếu không có gợi ý nào phù hợp. " +
            "Nếu câu hỏi của người dùng KHÔNG liên quan tới việc gợi ý món ăn/nhà hàng (chào hỏi, " +
            "hỏi han chung chung, hỏi về ngân sách/calo/chế độ ăn của một món đã nhắc trước đó, v.v.) " +
            "thì trả lời bình thường trong replyText và để 2 danh sách ID rỗng.\n\n" +
            "recommendedFoodIds: danh sách các ID món ăn (số nguyên, lấy từ tiền tố [F<id>] đứng " +
            "trước mỗi món trong DANH SÁCH DỮ LIỆU bên dưới) mà bạn thực sự muốn gợi ý cho câu hỏi " +
            "này. recommendedRestaurantIds: tương tự nhưng lấy từ tiền tố [R<id>] đứng trước mỗi " +
            "nhà hàng. CHỈ dùng ID CÓ THẬT xuất hiện trong danh sách dữ liệu — TUYỆT ĐỐI KHÔNG bịa " +
            "ID. Nếu không có món/nhà hàng nào phù hợp, để danh sách rỗng ([]), đừng cố nhét ID " +
            "không liên quan.\n" +
            "Không cần điền cả 2 danh sách cùng lúc — nếu câu hỏi chỉ về món ăn nói chung (không " +
            "gắn với một nhà hàng/vị trí cụ thể) thì chỉ cần recommendedFoodIds; nếu câu hỏi về " +
            "nhà hàng gần vị trí thì đã có mục VỊ TRÍ NGƯỜI DÙNG/NHÀ HÀNG GẦN VỊ TRÍ riêng xử lý " +
            "việc đó rồi (mục đó dùng dữ liệu tính toán chính xác, không cần bạn lặp lại ID nhà hàng " +
            "trong trường hợp này, để recommendedRestaurantIds rỗng).\n\n" +
            "CHỈ được gợi ý món ăn/nhà hàng CÓ TRONG danh sách dữ liệu bên dưới. " +
            "TUYỆT ĐỐI KHÔNG bịa ra món ăn, nhà hàng, giá cả không có trong danh sách.\n" +
            "CHỈ đưa thông tin về ngân sách, calo, chế độ ăn KHI NGƯỜI DÙNG HỎI CỤ THỂ, và đưa vào " +
            "replyText dưới dạng câu văn (không có card riêng cho việc này).\n" +
            "Nếu người dùng hỏi về nhà hàng/món ăn GẦN HỌ: nếu có mục VỊ TRÍ NGƯỜI DÙNG/NHÀ HÀNG GẦN VỊ TRÍ " +
            "bên dưới thì replyText chỉ cần 1 câu dẫn ngắn (vd \"Dưới đây là danh sách quán gần bạn nhất:\"), " +
            "danh sách quán cụ thể đã được hệ thống tính toán sẵn và sẽ hiển thị dạng card; nếu KHÔNG có " +
            "mục đó, hãy nói rõ trong replyText là chưa nhận được vị trí của người dùng (trình duyệt chưa " +
            "cấp quyền định vị) thay vì bịa khoảng cách.\n" +
            "Nếu có LỊCH SỬ HỘI THOẠI bên dưới, hãy dùng nó để hiểu ngữ cảnh câu hỏi hiện tại " +
            "(ví dụ người dùng nói \"còn món khác thì sao\" nghĩa là tiếp nối câu hỏi trước đó).\n" +
            "Người dùng đang ở Việt Nam.\n" +
            "Hãy trả lời bằng tiếng Việt nếu người dùng hỏi tiếng Việt, tiếng Anh nếu hỏi tiếng Anh " +
            "(replyText viết theo ngôn ngữ đó).\n\n" +
            historyContext +
            locationContext +
            "DANH SÁCH DỮ LIỆU CÓ SẴN (mỗi món/nhà hàng có tiền tố [F<id>]/[R<id>] — dùng đúng số " +
            "id này khi điền recommendedFoodIds/recommendedRestaurantIds):\n" +
            foodRestaurantContext +
            "\nCÂU HỎI CỦA NGƯỜI DÙNG:\n" + userMessage;

        return systemInstruction;
    }

    public void deleteConversation(Integer id) {
        aiConversationRepository.deleteById(id);
    }
}
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
import com.foodrecommendation.dto.NearbyRestaurantDto;
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

        String foodRestaurantContext = buildFoodRestaurantContext(conversation.getUserId());
        List<NearbyRestaurantDto> nearbyRestaurants = findNearbyRestaurants(
                conversation.getLatitude(), conversation.getLongitude(), userMessage);
        boolean hasLocation = conversation.getLatitude() != null && conversation.getLongitude() != null;
        String locationContext = buildLocationContext(nearbyRestaurants, hasLocation);
        String historyContext = buildConversationHistoryContext(conversation.getSessionId());
        String aiResponse = callGeminiAPI(userMessage, foodRestaurantContext, locationContext, historyContext);

        conversation.setUserMessage(userMessage);
        conversation.setAiResponse(aiResponse);
        conversation.setNearbyRestaurants(nearbyRestaurants);
        return aiConversationRepository.save(conversation);
    }

    // =========================================================
    // Ngữ cảnh món ăn / nhà hàng - có dùng RecommendationEngine
    // =========================================================

    private String buildFoodRestaurantContext(Integer userId) {
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
                RestaurantFood rf = findAvailableRestaurantFood(food.getFoodId());
                Double price = rf != null && rf.getPrice() != null ? rf.getPrice() : food.getPrice();
                Restaurant restaurant = rf != null ? restaurantRepository.findById(rf.getRestaurantId()).orElse(null) : null;

                context.append(String.format("- %s | giá: %.0f VND | loại: %s",
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
                context.append(String.format("- %s | rating: %.1f sao | địa chỉ: %s | giờ mở: %s\n",
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
        if (lat == null || lng == null) {
            return Collections.emptyList();
        }

        List<Restaurant> candidates = restaurantRepository.findByDistance(lat, lng, NEARBY_RADIUS_KM);
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }

        List<Food> allFoods = foodRepository.findAll();
        String dishKeyword = extractDishKeyword(userMessage, allFoods);

        List<NearbyRestaurantDto> result = new ArrayList<>();
        for (Restaurant r : candidates) {
            if (r.getLatitude() == null || r.getLongitude() == null) continue;
            double distanceKm = haversineKm(lat, lng, r.getLatitude(), r.getLongitude());
            if (distanceKm > NEARBY_RADIUS_KM) continue; // findByDistance dùng bounding box, lọc lại cho chính xác

            // Người dùng hỏi 1 món cụ thể (vd "cơm tấm gần đây") -> chỉ giữ
            // nhà hàng thật sự có bán món đó, tránh trả về lạc đề như trước.
            if (dishKeyword != null && !restaurantServesDish(r.getRestaurantId(), dishKeyword)) {
                continue;
            }

            result.add(new NearbyRestaurantDto(
                    r.getRestaurantId(),
                    r.getName(),
                    r.getRating(),
                    Math.round(distanceKm * 10.0) / 10.0,
                    r.getAddress(),
                    r.getOpeningHours()
            ));
        }
        result.sort(Comparator.comparingDouble(NearbyRestaurantDto::getDistanceKm));
        if (result.size() > MAX_RESTAURANTS_IN_CONTEXT) {
            return result.subList(0, MAX_RESTAURANTS_IN_CONTEXT);
        }
        return result;
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
        sb.append("NHÀ HÀNG GẦN VỊ TRÍ NGƯỜI DÙNG (trong bán kính ")
          .append((int) NEARBY_RADIUS_KM).append("km):\n");
        for (NearbyRestaurantDto r : nearbyRestaurants) {
            sb.append(String.format("- %s | cách %.1f km | rating: %.1f sao | địa chỉ: %s | giờ mở: %s\n",
                    r.getName(),
                    r.getDistanceKm(),
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

    private String callGeminiAPI(String userMessage, String foodRestaurantContext, String locationContext, String historyContext) {
        try {
            String url = geminiBaseUrl + "?key=" + geminiApiKey;
            String prompt = buildPrompt(userMessage, foodRestaurantContext, locationContext, historyContext);

            Map<String, Object> content = Map.of(
                "parts", new Object[]{ Map.of("text", prompt) }
            );
            Map<String, Object> requestBody = Map.of("contents", new Object[]{ content });

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

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
                                return (String) parts.get(0).get("text");
                            }
                        }
                    }
                }
            }

            return "Xin lỗi, tôi không thể trả lời câu hỏi này.";

        } catch (Exception e) {
            System.err.println("Error calling Gemini API: " + e.getMessage());
            e.printStackTrace();
            return "Xin lỗi, hệ thống đang gặp sự cố.";
        }
    }

    private String buildPrompt(String userMessage, String foodRestaurantContext, String locationContext, String historyContext) {
        String systemInstruction =
            "Bạn là FoodAI – trợ lý ẩm thực thông minh.\n" +
            "Hãy trả lời NGẮN GỌN, súc tích, không đoạn văn dài.\n" +
            "Mỗi gợi ý chỉ 1-2 dòng, có icon, tên món, giá, nhà hàng, địa chỉ, giờ mở.\n" +
            "CHỈ đưa thông tin về ngân sách, calo, chế độ ăn KHI NGƯỜI DÙNG HỎI CỤ THỂ.\n" +
            "KHÔNG TỰ Ý THÊM thông tin không được hỏi (ví dụ: ngân sách, sở thích, dietary preferences).\n" +
            "CHỈ được gợi ý món ăn/nhà hàng CÓ TRONG danh sách dữ liệu bên dưới. " +
            "TUYỆT ĐỐI KHÔNG bịa ra món ăn, nhà hàng, giá cả không có trong danh sách.\n" +
            "Nếu danh sách không có món nào phù hợp với câu hỏi, hãy nói thật là chưa tìm thấy, " +
            "đừng tự nghĩ ra thông tin.\n" +
            "Nếu người dùng hỏi về nhà hàng/món ăn GẦN HỌ: nếu có mục VỊ TRÍ NGƯỜI DÙNG/NHÀ HÀNG GẦN VỊ TRÍ " +
            "bên dưới thì dùng đúng danh sách đó để trả lời; nếu KHÔNG có mục đó, hãy nói rõ là chưa nhận " +
            "được vị trí của người dùng (trình duyệt chưa cấp quyền định vị) thay vì bịa khoảng cách.\n" +
            "Nếu có LỊCH SỬ HỘI THOẠI bên dưới, hãy dùng nó để hiểu ngữ cảnh câu hỏi hiện tại " +
            "(ví dụ người dùng nói \"còn món khác thì sao\" nghĩa là tiếp nối câu hỏi trước đó).\n" +
            "Người dùng đang ở Việt Nam.\n" +
            "Hãy trả lời bằng tiếng Việt nếu người dùng hỏi tiếng Việt, tiếng Anh nếu hỏi tiếng Anh.\n\n" +
            historyContext +
            locationContext +
            "DANH SÁCH DỮ LIỆU CÓ SẴN:\n" +
            foodRestaurantContext +
            "\nCÂU HỎI CỦA NGƯỜI DÙNG:\n" + userMessage;

        return systemInstruction;
    }

    public void deleteConversation(Integer id) {
        aiConversationRepository.deleteById(id);
    }
}
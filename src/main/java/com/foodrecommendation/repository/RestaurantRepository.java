package com.foodrecommendation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.foodrecommendation.entity.Restaurant;

@Repository
public interface RestaurantRepository
        extends JpaRepository<Restaurant, Integer> {

    // Tìm nhà hàng trong bán kính radius (km) từ tọa độ (lat, lng)
    // Lưu ý: đây là phép tính khoảng cách đơn giản, không chính xác cho mọi vùng địa lý
    // Nhưng đủ dùng cho demo
    @Query("SELECT r FROM Restaurant r WHERE " +
           "ABS(r.latitude - :lat) <= :radius / 111.0 " +
           "AND ABS(r.longitude - :lng) * COS(RADIANS(:lat)) <= :radius / (111.0 * COS(RADIANS(:lat)))")
    List<Restaurant> findByDistance(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("radius") Double radius // km
    );

    // Tìm nhà hàng theo rating (tối thiểu)
    List<Restaurant> findByRatingGreaterThanEqual(Double minRating);

    // Tìm nhà hàng có giờ mở cửa (openingHours không null)
    List<Restaurant> findByOpeningHoursIsNotNullOrOpeningHoursIsNotNull();

    // Tìm tất cả nhà hàng có rating
    List<Restaurant> findByRatingIsNotNull();

    List<Restaurant> findTop5ByOrderByRatingDesc();
}

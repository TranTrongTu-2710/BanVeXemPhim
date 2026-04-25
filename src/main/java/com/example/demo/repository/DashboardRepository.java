package com.example.demo.repository;

import com.example.demo.response.FoodSaleResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DashboardRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<FoodSaleResponse> getTopFoodSales(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = 
            "SELECT " +
            "    f.id AS foodItemId, " +
            "    f.name AS foodItemName, " +
            "    SUM(combined.quantity) AS totalQuantity, " +
            "    SUM(combined.quantity * combined.price) AS totalRevenue " +
            "FROM ( " +
            "    SELECT foi.food_item_id, foi.quantity, foi.price_at_purchase AS price " + // SỬA: price -> price_at_purchase
            "    FROM food_order_items foi " +
            "    JOIN food_orders fo ON foi.food_order_id = fo.id " +
            "    WHERE fo.status = 'COMPLETED' " +
            "    AND fo.created_at BETWEEN :startDate AND :endDate " +
            "    UNION ALL " +
            "    SELECT bf.food_item_id, bf.quantity, bf.price " + // BookingFood dùng cột price là đúng
            "    FROM booking_food bf " +
            "    JOIN bookings b ON bf.booking_id = b.id " +
            "    WHERE b.booking_status IN ('confirmed', 'completed') " +
            "    AND b.booking_date BETWEEN :startDate AND :endDate " +
            ") AS combined " +
            "JOIN food_items f ON combined.food_item_id = f.id " +
            "GROUP BY f.id, f.name " +
            "ORDER BY totalQuantity DESC " +
            "LIMIT 5";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        List<Object[]> results = query.getResultList();
        List<FoodSaleResponse> response = new ArrayList<>();

        for (Object[] row : results) {
            Integer id = ((Number) row[0]).intValue();
            String name = (String) row[1];
            Long quantity = ((Number) row[2]).longValue();
            BigDecimal revenue = (BigDecimal) row[3];
            
            response.add(new FoodSaleResponse(id, name, quantity, revenue));
        }

        return response;
    }
}

package com.example.demo.repository;

import com.example.demo.model.FoodOrderItem;
import com.example.demo.response.FoodSaleResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FoodOrderItemRepository extends JpaRepository<FoodOrderItem, Integer> {

    @Query("SELECT new com.example.demo.response.FoodSaleResponse(fi.id, fi.name, SUM(foi.quantity), SUM(foi.priceAtPurchase * foi.quantity)) " +
           "FROM FoodOrderItem foi " +
           "JOIN foi.foodItem fi " +
           "JOIN foi.foodOrder fo " +
           "WHERE fo.status = com.example.demo.model.FoodOrder.OrderStatus.COMPLETED AND fo.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY fi.id, fi.name " +
           "ORDER BY SUM(foi.quantity) DESC")
    List<FoodSaleResponse> findFoodSalesByDateRange(@Param("startDate") LocalDateTime startDateTime, @Param("endDate") LocalDateTime endDateTime, Pageable pageable);

    @Query("SELECT SUM(foi.quantity) FROM FoodOrderItem foi JOIN foi.foodOrder fo WHERE fo.status = com.example.demo.model.FoodOrder.OrderStatus.COMPLETED AND fo.createdAt BETWEEN :startDate AND :endDate")
    Long sumTotalQuantityByDateRange(@Param("startDate") LocalDateTime startDateTime, @Param("endDate") LocalDateTime endDateTime);
}

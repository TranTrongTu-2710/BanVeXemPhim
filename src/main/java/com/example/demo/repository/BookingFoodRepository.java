package com.example.demo.repository;

import com.example.demo.model.BookingFood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface BookingFoodRepository extends JpaRepository<BookingFood, Integer> {

    // Tính tổng số lượng đồ ăn bán kèm theo Booking (chỉ tính các booking đã xác nhận/hoàn thành)
    @Query("SELECT SUM(bf.quantity) FROM BookingFood bf " +
           "WHERE bf.booking.bookingStatus IN ('confirmed', 'completed') " +
           "AND bf.booking.bookingDate BETWEEN :startDate AND :endDate")
    Long sumTotalQuantityByDateRange(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);
}

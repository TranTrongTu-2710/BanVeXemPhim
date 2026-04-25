package com.example.demo.repository;

import com.example.demo.model.Booking;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    Optional<Booking> findByBookingCode(String bookingCode);
    List<Booking> findByUserId(Integer userId);
    List<Booking> findByShowtimeId(Integer showtimeId);

    @Query("SELECT FUNCTION('DATE', b.bookingDate), SUM(b.finalAmount) " +
           "FROM Booking b " +
           "WHERE b.bookingStatus IN (com.example.demo.model.Booking.BookingStatus.confirmed, com.example.demo.model.Booking.BookingStatus.completed) " +
           "AND FUNCTION('DATE', b.bookingDate) BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE', b.bookingDate) " +
           "ORDER BY FUNCTION('DATE', b.bookingDate)")
    List<Object[]> findRevenueByDayRaw(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(b.finalAmount), 0) FROM Booking b " +
           "WHERE b.bookingStatus IN (com.example.demo.model.Booking.BookingStatus.confirmed, com.example.demo.model.Booking.BookingStatus.completed) " +
           "AND b.bookingDate BETWEEN :startDate AND :endDate")
    BigDecimal findTotalRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Đếm số đơn hàng (giữ lại nếu cần dùng cho mục đích khác)
    @Query("SELECT COUNT(b) FROM Booking b " +
           "WHERE b.bookingStatus IN (com.example.demo.model.Booking.BookingStatus.confirmed, com.example.demo.model.Booking.BookingStatus.completed) " +
           "AND b.bookingDate BETWEEN :startDate AND :endDate")
    Long countConfirmedBookingsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // MỚI: Đếm tổng số vé (ghế) đã bán
    @Query("SELECT COUNT(bs) FROM BookingSeat bs " +
           "JOIN bs.booking b " +
           "WHERE b.bookingStatus IN (com.example.demo.model.Booking.BookingStatus.confirmed, com.example.demo.model.Booking.BookingStatus.completed) " +
           "AND b.bookingDate BETWEEN :startDate AND :endDate")
    Long countSoldTicketsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    List<Booking> findTop5ByOrderByCreatedAtDesc();
}

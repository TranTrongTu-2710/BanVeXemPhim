package com.example.demo.repository;

import com.example.demo.model.SeatHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SeatHoldRepository extends JpaRepository<SeatHold, Integer> {
    Optional<SeatHold> findByShowtimeIdAndSeatId(Integer showtimeId, Integer seatId);
    List<SeatHold> findByHoldUntilBefore(LocalDateTime now);
    void deleteByShowtimeIdAndSeatIdIn(Integer showtimeId, List<Integer> seatIds);

    /**
     * Lấy ra một Set các ID của ghế đang được giữ (chưa hết hạn) cho một suất chiếu cụ thể.
     */
    @Query("SELECT sh.seat.id FROM SeatHold sh " +
           "WHERE sh.showtime.id = :showtimeId AND sh.holdUntil > :now")
    Set<Integer> findHeldSeatIdsByShowtimeId(@Param("showtimeId") Integer showtimeId, @Param("now") LocalDateTime now);
}

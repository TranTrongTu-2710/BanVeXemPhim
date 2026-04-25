package com.example.demo.repository;

import com.example.demo.model.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Integer> {

    @Query("SELECT bs.seat.id FROM BookingSeat bs WHERE bs.booking.showtime.id = :showtimeId AND bs.booking.bookingStatus IN ('CONFIRMED', 'COMPLETED')")
    Set<Integer> findBookedSeatIdsByShowtime(@Param("showtimeId") Integer showtimeId);
}

package com.example.demo.repository;

import com.example.demo.model.SeatPrice;
import com.example.demo.model.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatPriceRepository extends JpaRepository<SeatPrice, Integer> {
    List<SeatPrice> findByShowtime(Showtime showtime);
    List<SeatPrice> findByShowtimeId(Integer showtimeId);
}

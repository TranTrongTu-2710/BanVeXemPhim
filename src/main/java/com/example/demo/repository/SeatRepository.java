package com.example.demo.repository;

import com.example.demo.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {
    List<Seat> findByScreenId(Integer screenId);
    boolean existsByScreenIdAndRowNameAndSeatNumber(Integer screenId, String rowName, Integer seatNumber);
}

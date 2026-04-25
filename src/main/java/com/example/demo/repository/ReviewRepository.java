package com.example.demo.repository;

import com.example.demo.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByMovieId(Integer movieId);
    List<Review> findByUserId(Integer userId);
    boolean existsByUserIdAndMovieId(Integer userId, Integer movieId);
}

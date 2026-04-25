package com.example.demo.repository;

import com.example.demo.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {
    // --- Dành cho Admin ---
    List<Movie> findByStatus(Movie.MovieStatus status);

    // --- Dành cho Public ---
    Optional<Movie> findByIdAndIsActiveTrue(Integer id);
    List<Movie> findByStatusAndIsActiveTrue(Movie.MovieStatus status);
    List<Movie> findByIsActiveTrue();

    /**
     * Lấy danh sách các phim đang hoạt động và có ít nhất một suất chiếu
     * trong ngày hôm nay tại một rạp cụ thể.
     * (Đã bỏ điều kiện m.status = 'now_showing')
     */
    @Query("SELECT DISTINCT m FROM Movie m JOIN m.showtimes s " +
           "WHERE m.isActive = true " +
           "AND s.screen.cinema.id = :cinemaId " +
           "AND s.showDate = :today")
    List<Movie> findScheduledMoviesByCinemaAndDate(
            @Param("cinemaId") Integer cinemaId,
            @Param("today") LocalDate today
    );
}

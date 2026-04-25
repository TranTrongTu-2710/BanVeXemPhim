package com.example.demo.repository;

import com.example.demo.model.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CinemaRepository extends JpaRepository<Cinema, Integer> {
    List<Cinema> findByCity(String city);
    List<Cinema> findByIsActive(boolean isActive);

    /**
     * Tìm tất cả các rạp đang hoạt động và có suất chiếu của một phim cụ thể
     * từ ngày hôm nay trở đi.
     */
    @Query("SELECT DISTINCT c FROM Cinema c JOIN c.screens s JOIN s.showtimes st " +
           "WHERE c.isActive = true " +
           "AND st.movie.id = :movieId " +
           "AND st.showDate >= :today")
    List<Cinema> findActiveCinemasByMovieAndDate(
            @Param("movieId") Integer movieId,
            @Param("today") LocalDate today
    );
}

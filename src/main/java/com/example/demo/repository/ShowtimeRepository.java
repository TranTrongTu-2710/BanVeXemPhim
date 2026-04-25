package com.example.demo.repository;

import com.example.demo.model.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Integer> {

    List<Showtime> findByMovieId(Integer movieId);

    List<Showtime> findByScreenId(Integer screenId);

    @Query("SELECT s FROM Showtime s WHERE s.movie.id = :movieId AND s.showDate = :date")
    List<Showtime> findByMovieIdAndShowDate(@Param("movieId") Integer movieId, @Param("date") LocalDate date);
    @Query("SELECT s FROM Showtime s " +
            "WHERE s.movie.id = :movieId " +
            "AND s.showDate > :date")
    List<Showtime> findByMovieIdAndAfterDate(Integer movieId, @Param("date") LocalDate date);
    /**
     * Tìm tất cả các suất chiếu (ngoại trừ suất có ID cho trước, dùng khi update)
     * trong cùng một phòng chiếu và cùng một ngày.
     */
    @Query("SELECT s FROM Showtime s WHERE s.screen.id = :screenId AND s.showDate = :showDate AND s.id <> :excludeShowtimeId")
    List<Showtime> findOverlappingShowtimes(
            @Param("screenId") Integer screenId,
            @Param("showDate") LocalDate showDate,
            @Param("excludeShowtimeId") Integer excludeShowtimeId
    );
}

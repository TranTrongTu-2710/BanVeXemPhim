package com.example.demo.services;

import com.example.demo.model.Showtime;
import com.example.demo.request.showtime.CreateShowtimeRequest;
import com.example.demo.request.showtime.UpdateShowtimeRequest;
import com.example.demo.response.ShowtimeResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface ShowtimeService {
    // Các phương thức trả về DTO cho API
    ShowtimeResponseDTO getShowtimeDTOById(Integer id);
    List<ShowtimeResponseDTO> getAllShowtimesDTO();
    List<ShowtimeResponseDTO> getShowtimesDTOByMovieAndDate(Integer movieId, LocalDate date);

    // Các phương thức trả về Entity để dùng nội bộ (nếu cần)
    Showtime getShowtimeById(Integer id);

    // Các phương thức CUD
    Showtime createShowtime(CreateShowtimeRequest request);
    Showtime updateShowtime(Integer id, UpdateShowtimeRequest request);
    void deleteShowtime(Integer id);

    List<ShowtimeResponseDTO> getShowtimesDTOByMovie(Integer movieId, LocalDate date);
}

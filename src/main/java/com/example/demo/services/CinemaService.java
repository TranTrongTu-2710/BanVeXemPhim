package com.example.demo.services;

import com.example.demo.model.Cinema;
import com.example.demo.request.cinema.CreateCinemaRequest;
import com.example.demo.request.cinema.UpdateCinemaRequest;
import com.example.demo.response.CinemaWithShowtimesDTO;

import java.util.List;

public interface CinemaService {
    Cinema createCinema(CreateCinemaRequest request);
    Cinema getCinemaById(Integer id);
    List<Cinema> getAllCinemas();
    List<Cinema> getActiveCinemas();
    Cinema updateCinema(Integer id, UpdateCinemaRequest request);
    void deleteCinema(Integer id);

    // Phương thức mới
    List<CinemaWithShowtimesDTO> findCinemasByMovie(Integer movieId);
}

package com.example.demo.services;

import com.example.demo.model.Movie;
import com.example.demo.request.movie.CreateMovieRequest;
import com.example.demo.request.movie.UpdateMovieRequest;
import com.example.demo.response.MovieResponseDTO;

import java.util.List;

public interface MovieService {
    // --- Admin methods ---
    Movie createMovie(CreateMovieRequest request);
    Movie updateMovie(Integer id, UpdateMovieRequest request);
    void deleteMovie(Integer id);
    Movie getMovieByIdForAdmin(Integer id);
    List<Movie> getAllMoviesForAdmin();

    // --- Public methods (trả về DTO) ---
    MovieResponseDTO getActiveMovieByIdDTO(Integer id);
    List<MovieResponseDTO> getAllActiveMoviesDTO();
    List<MovieResponseDTO> getActiveMoviesByStatusDTO(Movie.MovieStatus status);

    // --- Staff methods (trả về DTO) ---
    List<MovieResponseDTO> getScheduledMoviesForStaffCinemaTodayDTO();
}

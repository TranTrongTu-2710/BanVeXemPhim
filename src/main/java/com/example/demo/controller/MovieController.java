package com.example.demo.controller;

import com.example.demo.model.Movie;
import com.example.demo.request.movie.CreateMovieRequest;
import com.example.demo.request.movie.UpdateMovieRequest;
import com.example.demo.response.MovieResponseDTO;
import com.example.demo.services.FileStorageService;
import com.example.demo.services.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;
    private final FileStorageService fileStorageService;

    // --- Public Routes ---

    @GetMapping
    public ResponseEntity<List<MovieResponseDTO>> getPublicMovies(@RequestParam(required = false) String status) {
        List<MovieResponseDTO> movies;
        if (status != null) {
            try {
                Movie.MovieStatus movieStatus = Movie.MovieStatus.valueOf(status.toLowerCase());
                movies = movieService.getActiveMoviesByStatusDTO(movieStatus);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            movies = movieService.getAllActiveMoviesDTO();
        }
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponseDTO> getPublicMovieById(@PathVariable Integer id) {
        return ResponseEntity.ok(movieService.getActiveMovieByIdDTO(id));
    }

    // --- Staff & Admin Route ---

    @GetMapping("/staff/scheduled-today")
    public ResponseEntity<List<MovieResponseDTO>> getScheduledMoviesForStaff() {
        return ResponseEntity.ok(movieService.getScheduledMoviesForStaffCinemaTodayDTO());
    }

    // --- Admin Routes ---
    @CacheEvict(value = "movies", allEntries = true)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Movie> createMovie(@Valid @ModelAttribute CreateMovieRequest request) {
        // File ảnh đã nằm trong request.getPosterUrl()
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.createMovie(request));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<Movie>> getAllMoviesForAdmin() {
        return ResponseEntity.ok(movieService.getAllMoviesForAdmin());
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<Movie> getMovieByIdForAdmin(@PathVariable Integer id) {
        return ResponseEntity.ok(movieService.getMovieByIdForAdmin(id));
    }

    @CacheEvict(value = "movies", allEntries = true)
    // Sửa lỗi "POST not supported": Cho phép cả PUT và POST
    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.POST}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Movie> updateMovie(@PathVariable Integer id, @Valid @ModelAttribute UpdateMovieRequest request) {
        // Logic: Nếu có ảnh mới (request.getPosterUrl() không rỗng) -> Xóa ảnh cũ
        if (request.getPosterUrl() != null && !request.getPosterUrl().isEmpty()) {
            Movie oldMovie = movieService.getMovieByIdForAdmin(id);
            if (oldMovie != null && oldMovie.getPosterUrl() != null) {
                fileStorageService.deleteFile(oldMovie.getPosterUrl());
            }
        }
        return ResponseEntity.ok(movieService.updateMovie(id, request));
    }

    @CacheEvict(value = "movies", allEntries = true)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteMovie(@PathVariable Integer id) {
        // Logic: Xóa phim -> Xóa luôn ảnh
        Movie movie = movieService.getMovieByIdForAdmin(id);
        if (movie != null && movie.getPosterUrl() != null) {
            fileStorageService.deleteFile(movie.getPosterUrl());
        }
        movieService.deleteMovie(id);
        return ResponseEntity.ok(Map.of("message", "Movie deactivated successfully."));
    }
}

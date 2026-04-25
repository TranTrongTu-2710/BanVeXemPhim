package com.example.demo.controller;

import com.example.demo.model.Cinema;
import com.example.demo.request.cinema.CreateCinemaRequest;
import com.example.demo.request.cinema.UpdateCinemaRequest;
import com.example.demo.response.CinemaWithShowtimesDTO;
import com.example.demo.services.CinemaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cinemas")
@RequiredArgsConstructor
public class CinemaController {

    private final CinemaService cinemaService;

    // --- Public Routes ---

    @GetMapping
    public ResponseEntity<List<Cinema>> getActiveCinemas() {
        return ResponseEntity.ok(cinemaService.getActiveCinemas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cinema> getCinemaById(@PathVariable Integer id) {
        return ResponseEntity.ok(cinemaService.getCinemaById(id));
    }

    @GetMapping("/by-movie/{movieId}")
    public ResponseEntity<List<CinemaWithShowtimesDTO>> getCinemasByMovie(@PathVariable Integer movieId) {
        return ResponseEntity.ok(cinemaService.findCinemasByMovie(movieId));
    }

    // --- Admin Routes ---

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Cinema> createCinema(@Valid @RequestBody CreateCinemaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cinemaService.createCinema(request));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Cinema>> getAllCinemas() {
        return ResponseEntity.ok(cinemaService.getAllCinemas());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cinema> updateCinema(@PathVariable Integer id, @Valid @RequestBody UpdateCinemaRequest request) {
        return ResponseEntity.ok(cinemaService.updateCinema(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCinema(@PathVariable Integer id) {
        cinemaService.deleteCinema(id);
        return ResponseEntity.ok(Map.of("message", "Cinema deactivated successfully."));
    }
}

package com.example.demo.controller;

import com.example.demo.model.Showtime;
import com.example.demo.request.showtime.CreateShowtimeRequest;
import com.example.demo.request.showtime.UpdateShowtimeRequest;
import com.example.demo.response.SeatStatusResponse;
import com.example.demo.response.ShowtimeResponseDTO;
import com.example.demo.services.SeatSelectionService;
import com.example.demo.services.ShowtimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;
    private final SeatSelectionService seatSelectionService; // Inject the correct service

    // --- Public Routes ---

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ShowtimeResponseDTO>> getShowtimesByMovieAndDate(
            @PathVariable Integer movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(showtimeService.getShowtimesDTOByMovieAndDate(movieId, date));
    }
    @GetMapping("/bymovie/{movieId}")
    public ResponseEntity<List<ShowtimeResponseDTO>> getShowtimesByMovieAndDate(
            @PathVariable Integer movieId) {
        LocalDate date = LocalDate.now();
        return ResponseEntity.ok(showtimeService.getShowtimesDTOByMovie(movieId, date));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ShowtimeResponseDTO> getShowtimeById(@PathVariable Integer id) {
        return ResponseEntity.ok(showtimeService.getShowtimeDTOById(id));
    }

    /**
     * Lấy danh sách tất cả các ghế của một suất chiếu, bao gồm trạng thái
     * (trống, đã bán, đang được giữ) từ cả DB và Redis.
     * @param id ID của suất chiếu
     * @return Danh sách ghế với trạng thái
     */
    @GetMapping("/{id}/seats")
    public ResponseEntity<List<SeatStatusResponse>> getSeatsForShowtime(@PathVariable Integer id) {
        return ResponseEntity.ok(seatSelectionService.getSeatStatusesForShowtime(id));
    }

    // --- Admin Routes ---

    @PostMapping
    public ResponseEntity<Showtime> createShowtime(@Valid @RequestBody CreateShowtimeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(showtimeService.createShowtime(request));
    }

    @GetMapping
    public ResponseEntity<List<ShowtimeResponseDTO>> getAllShowtimes() {
        return ResponseEntity.ok(showtimeService.getAllShowtimesDTO());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Showtime> updateShowtime(@PathVariable Integer id, @Valid @RequestBody UpdateShowtimeRequest request) {
        return ResponseEntity.ok(showtimeService.updateShowtime(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteShowtime(@PathVariable Integer id) {
        showtimeService.deleteShowtime(id);
        return ResponseEntity.ok(Map.of("message", "Showtime cancelled successfully."));
    }
}

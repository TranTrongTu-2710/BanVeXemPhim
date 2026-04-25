package com.example.demo.controller;

import com.example.demo.model.Seat;
import com.example.demo.request.seat.CreateSeatRequest;
import com.example.demo.request.seat.UpdateSeatRequest;
import com.example.demo.response.SeatResponseDTO;
import com.example.demo.response.SeatStatusResponse;
import com.example.demo.services.SeatSelectionService;
import com.example.demo.services.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/seats") // Đổi path cho thống nhất với các controller khác (thường là /seats thay vì /api/seats nếu cấu hình global prefix)
@RequiredArgsConstructor
public class SeatController {

    private final SeatSelectionService seatSelectionService;
    private final SeatService seatService;

    // --- Public/User Endpoints ---

    @GetMapping("/showtime/{showtimeId}")
    public ResponseEntity<List<SeatStatusResponse>> getSeatsForShowtime(@PathVariable Integer showtimeId) {
        return ResponseEntity.ok(seatSelectionService.getSeatStatusesForShowtime(showtimeId));
    }

    @GetMapping("/screen/{screenId}")
    public ResponseEntity<List<SeatResponseDTO>> getSeatsByScreen(@PathVariable Integer screenId) {
        List<Seat> seats = seatService.getSeatsByScreen(screenId);
        List<SeatResponseDTO> response = seats.stream()
                .map(SeatResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // --- Admin Endpoints ---

    @PostMapping
    public ResponseEntity<SeatResponseDTO> createSeat(@Valid @RequestBody CreateSeatRequest request) {
        Seat seat = seatService.createSeat(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SeatResponseDTO(seat));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SeatResponseDTO> updateSeat(@PathVariable Integer id, @RequestBody UpdateSeatRequest request) {
        Seat seat = seatService.updateSeat(id, request);
        return ResponseEntity.ok(new SeatResponseDTO(seat));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeat(@PathVariable Integer id) {
        seatService.deleteSeat(id);
        return ResponseEntity.noContent().build();
    }
}

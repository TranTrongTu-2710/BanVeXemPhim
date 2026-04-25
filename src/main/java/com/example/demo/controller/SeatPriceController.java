package com.example.demo.controller;

import com.example.demo.model.SeatPrice;
import com.example.demo.request.seatprice.CreateSeatPriceRequest;
import com.example.demo.request.seatprice.UpdateSeatPriceRequest;
import com.example.demo.services.SeatPriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seat-prices")
@RequiredArgsConstructor
public class SeatPriceController {

    private final SeatPriceService seatPriceService;

    // --- Public Routes ---

    @GetMapping("/showtime/{showtimeId}")
    public ResponseEntity<List<SeatPrice>> getSeatPricesByShowtime(@PathVariable Integer showtimeId) {
        return ResponseEntity.ok(seatPriceService.getSeatPricesByShowtime(showtimeId));
    }

    // --- Admin Routes ---

    @PostMapping
    public ResponseEntity<SeatPrice> createSeatPrice(@Valid @RequestBody CreateSeatPriceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(seatPriceService.createSeatPrice(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeatPrice> getSeatPriceById(@PathVariable Integer id) {
        return ResponseEntity.ok(seatPriceService.getSeatPriceById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SeatPrice> updateSeatPrice(@PathVariable Integer id, @Valid @RequestBody UpdateSeatPriceRequest request) {
        return ResponseEntity.ok(seatPriceService.updateSeatPrice(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeatPrice(@PathVariable Integer id) {
        seatPriceService.deleteSeatPrice(id);
        return ResponseEntity.noContent().build();
    }
}

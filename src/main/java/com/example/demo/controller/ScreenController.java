package com.example.demo.controller;

import com.example.demo.model.Screen;
import com.example.demo.request.screen.CreateScreenRequest;
import com.example.demo.request.screen.UpdateScreenRequest;
import com.example.demo.services.ScreenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/screens")
@RequiredArgsConstructor
public class ScreenController {

    private final ScreenService screenService;

    // --- Public Routes ---

    @GetMapping("/cinema/{cinemaId}")
    public ResponseEntity<List<Screen>> getScreensByCinema(@PathVariable Integer cinemaId) {
        return ResponseEntity.ok(screenService.getScreensByCinema(cinemaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Screen> getScreenById(@PathVariable Integer id) {
        return ResponseEntity.ok(screenService.getScreenById(id));
    }

    // --- Admin Routes ---

    @PostMapping
    public ResponseEntity<Screen> createScreen(@Valid @RequestBody CreateScreenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(screenService.createScreen(request));
    }

    @GetMapping
    public ResponseEntity<List<Screen>> getAllScreens() {
        return ResponseEntity.ok(screenService.getAllScreens());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Screen> updateScreen(@PathVariable Integer id, @Valid @RequestBody UpdateScreenRequest request) {
        return ResponseEntity.ok(screenService.updateScreen(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteScreen(@PathVariable Integer id) {
        screenService.deleteScreen(id);
        return ResponseEntity.ok(Map.of("message", "Screen deactivated successfully."));
    }
}

package com.example.demo.controller;

import com.example.demo.model.Promotion;
import com.example.demo.request.promotion.CreatePromotionRequest;
import com.example.demo.request.promotion.UpdatePromotionRequest;
import com.example.demo.services.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    // --- Public Routes ---

    // Đổi tên endpoint cũ thành /public để rõ ràng hơn
    @GetMapping("/public")
    public ResponseEntity<List<Promotion>> getActivePublicPromotions() {
        return ResponseEntity.ok(promotionService.getActivePublicPromotions());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Promotion> getPromotionByCode(@PathVariable String code) {
        return ResponseEntity.ok(promotionService.getPromotionByCode(code));
    }

    // --- Admin Routes ---

    // ... (các endpoint của admin không thay đổi)
    @PostMapping
    public ResponseEntity<Promotion> createPromotion(@Valid @RequestBody CreatePromotionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promotionService.createPromotion(request));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Promotion>> getAllPromotions() {
        return ResponseEntity.ok(promotionService.getAllPromotions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Promotion> getPromotionById(@PathVariable Integer id) {
        return ResponseEntity.ok(promotionService.getPromotionById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Promotion> updatePromotion(@PathVariable Integer id, @Valid @RequestBody UpdatePromotionRequest request) {
        return ResponseEntity.ok(promotionService.updatePromotion(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePromotion(@PathVariable Integer id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.ok(Map.of("message", "Promotion deactivated successfully."));
    }
}

package com.example.demo.controller;

import com.example.demo.model.FoodItem;
import com.example.demo.request.foodItem.CreateFoodItemRequest;
import com.example.demo.request.foodItem.UpdateFoodItemRequest;
import com.example.demo.services.FoodItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/foods")
@RequiredArgsConstructor
public class FoodItemController {

    private final FoodItemService foodItemService;


    // --- Public Routes ---

    @GetMapping
    public ResponseEntity<List<FoodItem>> getAvailableFoodItems() {
        return ResponseEntity.ok(foodItemService.getAvailableFoodItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodItem> getFoodItemById(@PathVariable Integer id) {
        return ResponseEntity.ok(foodItemService.getFoodItemById(id));
    }

    // --- Admin Routes ---

    @PostMapping
    public ResponseEntity<FoodItem> createFoodItem(@Valid @RequestBody CreateFoodItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(foodItemService.createFoodItem(request));
    }

    @GetMapping("/all")
    public ResponseEntity<List<FoodItem>> getAllFoodItems() {
        return ResponseEntity.ok(foodItemService.getAllFoodItems());
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodItem> updateFoodItem(@PathVariable Integer id, @Valid @RequestBody UpdateFoodItemRequest request) {
        return ResponseEntity.ok(foodItemService.updateFoodItem(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFoodItem(@PathVariable Integer id) {
        foodItemService.deleteFoodItem(id);
        return ResponseEntity.ok(Map.of("message", "Food item deactivated successfully."));
    }
}

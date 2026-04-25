package com.example.demo.controller;

import com.example.demo.request.food_order.CreateFoodOrderRequest;
import com.example.demo.response.FoodOrderResponseDTO;
import com.example.demo.services.FoodOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/food-orders")
@RequiredArgsConstructor
public class FoodOrderController {

    private final FoodOrderService foodOrderService;

    @PostMapping
    public ResponseEntity<FoodOrderResponseDTO> createFoodOrder(@Valid @RequestBody CreateFoodOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(foodOrderService.createFoodOrder(request));
    }

    @GetMapping
    public ResponseEntity<List<FoodOrderResponseDTO>> getAllFoodOrders() {
        return ResponseEntity.ok(foodOrderService.getAllFoodOrdersDTO());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodOrderResponseDTO> getFoodOrderById(@PathVariable Integer id) {
        return ResponseEntity.ok(foodOrderService.getFoodOrderDTOById(id));
    }
}

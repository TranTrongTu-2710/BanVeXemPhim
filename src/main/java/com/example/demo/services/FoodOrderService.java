package com.example.demo.services;

import com.example.demo.request.food_order.CreateFoodOrderRequest;
import com.example.demo.response.FoodOrderResponseDTO;

import java.util.List;

public interface FoodOrderService {
    FoodOrderResponseDTO createFoodOrder(CreateFoodOrderRequest request);
    List<FoodOrderResponseDTO> getAllFoodOrdersDTO();
    FoodOrderResponseDTO getFoodOrderDTOById(Integer id);
}

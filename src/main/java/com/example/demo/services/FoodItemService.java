package com.example.demo.services;

import com.example.demo.model.FoodItem;
import com.example.demo.request.foodItem.CreateFoodItemRequest;
import com.example.demo.request.foodItem.UpdateFoodItemRequest;

import java.util.List;

public interface FoodItemService {
    FoodItem createFoodItem(CreateFoodItemRequest request);
    FoodItem getFoodItemById(Integer id);
    List<FoodItem> getAllFoodItems();
    List<FoodItem> getAvailableFoodItems();
    FoodItem updateFoodItem(Integer id, UpdateFoodItemRequest request);
    void deleteFoodItem(Integer id);
}

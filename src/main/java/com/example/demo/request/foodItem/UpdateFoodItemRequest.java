package com.example.demo.request.foodItem;

import com.example.demo.model.FoodItem;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateFoodItemRequest {
    private String name;
    private String description;
    private FoodItem.FoodCategory category;
    @Positive
    private BigDecimal price;
    private String imageUrl;
    private Boolean isAvailable;
}

package com.example.demo.request.foodItem;

import com.example.demo.model.FoodItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateFoodItemRequest {
    @NotBlank
    private String name;
    private String description;
    @NotNull
    private FoodItem.FoodCategory category;
    @NotNull
    @Positive
    private BigDecimal price;
    private String imageUrl;
}

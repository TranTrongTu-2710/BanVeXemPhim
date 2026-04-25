package com.example.demo.request.food_order;

import com.example.demo.model.Booking;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class CreateFoodOrderRequest {
    @NotNull(message = "Cinema ID is required to place a food order")
    private Integer cinemaId;

    // Đã xóa trường userId

    @NotEmpty
    private List<FoodOrderItemRequest> foodItems;

    @NotNull
    private Booking.PaymentMethod paymentMethod;

    private String notes;

    @Data
    public static class FoodOrderItemRequest {
        @NotNull
        private Integer foodItemId;
        @NotNull
        @Positive
        private Integer quantity;
    }
}

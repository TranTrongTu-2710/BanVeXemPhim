package com.example.demo.response;

import com.example.demo.model.BookingFood;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookingFoodResponseDTO {
    private String foodItemName;
    private Integer quantity;
    private BigDecimal price; // Giá tại thời điểm mua

    public BookingFoodResponseDTO(BookingFood bookingFood) {
        if (bookingFood.getFoodItem() != null) {
            this.foodItemName = bookingFood.getFoodItem().getName();
        }
        this.quantity = bookingFood.getQuantity();
        this.price = bookingFood.getPrice();
    }
}

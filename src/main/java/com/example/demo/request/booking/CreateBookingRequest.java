package com.example.demo.request.booking;

import com.example.demo.model.Booking;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateBookingRequest {
    @NotNull
    private Integer showtimeId;

    @NotEmpty
    private List<Integer> seatIds;

    private List<FoodOrderItem> foodItems; // Optional

    @NotNull
    private Booking.PaymentMethod paymentMethod;

    private String promotionCode; // Optional
    private Integer pointsUsed; // Optional
    private String notes; // Optional
    
    // Thêm trường amount để nhận tổng tiền từ client
    private BigDecimal amount;

    @Data
    public static class FoodOrderItem {
        @NotNull
        private Integer foodItemId;
        @NotNull
        @Positive
        private Integer quantity;
    }
}

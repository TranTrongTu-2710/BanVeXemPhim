package com.example.demo.request;

import lombok.Data;
import java.util.List;

@Data
public class CreatePaymentRequest {
    private Integer showtimeId;
    private List<Integer> seatIds;
    private List<FoodItemRequest> foodItems;
    private String promotionCode;
    private Integer pointsUsed;
    private String notes;
}

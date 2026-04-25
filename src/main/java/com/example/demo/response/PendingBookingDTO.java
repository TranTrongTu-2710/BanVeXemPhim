package com.example.demo.response;

import com.example.demo.request.FoodItemRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingBookingDTO implements Serializable {
    private Integer userId;
    private Integer showtimeId;
    private List<Integer> seatIds;
    private List<FoodItemRequest> foodItems;
    private String promotionCode;
    private Integer pointsUsed;
    private String notes;
    
    // Các giá trị đã được tính toán và xác thực ở backend
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
}

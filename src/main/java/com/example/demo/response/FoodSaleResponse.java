package com.example.demo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodSaleResponse {
    private Integer foodItemId;
    private String foodItemName;
    private Long totalQuantitySold;
    private BigDecimal totalRevenue;
}

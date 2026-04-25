package com.example.demo.request.seatprice;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateSeatPriceRequest {
    private BigDecimal price;
}

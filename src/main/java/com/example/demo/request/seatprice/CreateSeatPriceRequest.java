package com.example.demo.request.seatprice;

import com.example.demo.model.Seat;
import com.example.demo.model.SeatPrice;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateSeatPriceRequest {
    @NotNull
    private Integer showtimeId;
    @NotNull
    private Seat.SeatType seatType;
    @NotNull
    private BigDecimal price;
}

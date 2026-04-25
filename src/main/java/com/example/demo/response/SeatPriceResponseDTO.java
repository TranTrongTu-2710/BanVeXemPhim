package com.example.demo.response;

import com.example.demo.model.Seat;
import com.example.demo.model.SeatPrice;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SeatPriceResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Seat.SeatType seatType;
    private BigDecimal price;

    public SeatPriceResponseDTO(SeatPrice seatPrice) {
        this.seatType = seatPrice.getSeatType();
        this.price = seatPrice.getPrice();
    }
}

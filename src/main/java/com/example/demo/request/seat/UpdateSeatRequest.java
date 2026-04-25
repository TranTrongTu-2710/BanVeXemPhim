package com.example.demo.request.seat;

import com.example.demo.model.Seat;
import lombok.Data;

@Data
public class UpdateSeatRequest {
    private String rowName;
    private Integer seatNumber;
    private Seat.SeatType seatType;
    private Boolean isActive;
}

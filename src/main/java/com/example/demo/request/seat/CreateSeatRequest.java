package com.example.demo.request.seat;

import com.example.demo.model.Seat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateSeatRequest {
    @NotNull(message = "Screen ID is required")
    private Integer screenId;

    @NotBlank(message = "Row name is required")
    private String rowName;

    @NotNull(message = "Seat number is required")
    private Integer seatNumber;

    @NotNull(message = "Seat type is required")
    private Seat.SeatType seatType;
}

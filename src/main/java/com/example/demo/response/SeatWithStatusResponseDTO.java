package com.example.demo.response;

import com.example.demo.model.Seat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatWithStatusResponseDTO {
    private Integer id;
    private String rowName;
    private Integer seatNumber;
    private Seat.SeatType seatType;
    private SeatStatus status;
    private BigDecimal price;

    public enum SeatStatus {
        AVAILABLE,
        BOOKED,
        HELD
    }
}

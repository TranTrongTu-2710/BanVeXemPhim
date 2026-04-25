package com.example.demo.response;

import com.example.demo.model.Seat;
import lombok.Data;

@Data
public class SeatResponseDTO {
    private Integer id;
    private String rowName;
    private Integer seatNumber;
    private Seat.SeatType seatType;

    public SeatResponseDTO(Seat seat) {
        this.id = seat.getId();
        this.rowName = seat.getRowName();
        this.seatNumber = seat.getSeatNumber();
        this.seatType = seat.getSeatType();
    }
}

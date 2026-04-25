package com.example.demo.response;

import com.example.demo.model.Seat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatStatusResponse {
    private Integer id;
    private String rowName;
    private Integer seatNumber;
    private Seat.SeatType seatType;
    private String status; // AVAILABLE, BOOKED, LOCKED
    private Double price;
    private Integer userId; // ID của người đang giữ ghế (nếu có)
}

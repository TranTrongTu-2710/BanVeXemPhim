package com.example.demo.request.showtime;

import com.example.demo.model.Showtime;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class UpdateShowtimeRequest {
    private LocalDate showDate;
    private LocalTime startTime;
    private Showtime.ShowtimeStatus status;
    private List<CreateShowtimeRequest.SeatPriceRequest> seatPrices;
}

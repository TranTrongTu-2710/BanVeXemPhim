package com.example.demo.request.showtime;

import com.example.demo.model.Seat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class CreateShowtimeRequest {
    @NotNull
    private Integer movieId;
    @NotNull
    private Integer screenId;
    @NotNull
    private LocalDate showDate;
    @NotNull
    private LocalTime startTime;

    @NotEmpty(message = "Seat prices cannot be empty")
    private List<SeatPriceRequest> seatPrices;

    @Data
    public static class SeatPriceRequest {
        @NotNull
        private Seat.SeatType seatType;
        @NotNull
        private BigDecimal price;
    }
}

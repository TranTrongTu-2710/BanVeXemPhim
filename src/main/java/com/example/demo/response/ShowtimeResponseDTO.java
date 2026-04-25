package com.example.demo.response;

import com.example.demo.model.Showtime;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ShowtimeResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer movieId; // Thêm trường movieId
    private String movieTitle; // Thêm trường movieTitle
    private LocalDate showDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Showtime.ShowtimeStatus status;
    private Integer availableSeats;
    private ScreenResponseDTO screen;
    private List<SeatPriceResponseDTO> seatPrices;

    public ShowtimeResponseDTO(Showtime showtime) {
        this.id = showtime.getId();
        this.showDate = showtime.getShowDate();
        this.startTime = showtime.getStartTime();
        this.endTime = showtime.getEndTime();
        this.status = showtime.getStatus();
        this.availableSeats = showtime.getAvailableSeats();
        
        if (showtime.getMovie() != null) {
            this.movieId = showtime.getMovie().getId();
            this.movieTitle = showtime.getMovie().getTitle();
        }

        if (showtime.getScreen() != null) {
            this.screen = new ScreenResponseDTO(showtime.getScreen());
        }

        if (showtime.getSeatPrices() != null) {
            this.seatPrices = showtime.getSeatPrices().stream()
                    .map(SeatPriceResponseDTO::new)
                    .collect(Collectors.toList());
        }
    }
}

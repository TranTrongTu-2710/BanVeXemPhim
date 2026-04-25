package com.example.demo.services.serviceIplm;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.request.showtime.CreateShowtimeRequest;
import com.example.demo.request.showtime.UpdateShowtimeRequest;
import com.example.demo.response.ShowtimeResponseDTO;
import com.example.demo.services.ShowtimeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowtimeServiceImpl implements ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;

    @Override
    public ShowtimeResponseDTO getShowtimeDTOById(Integer id) {
        Showtime showtime = getShowtimeById(id);
        return new ShowtimeResponseDTO(showtime);
    }

    @Override
    public List<ShowtimeResponseDTO> getAllShowtimesDTO() {
        return showtimeRepository.findAll().stream()
                .map(ShowtimeResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShowtimeResponseDTO> getShowtimesDTOByMovieAndDate(Integer movieId, LocalDate date) {
        return showtimeRepository.findByMovieIdAndShowDate(movieId, date).stream()
                .map(ShowtimeResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public Showtime getShowtimeById(Integer id) {
        return showtimeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Showtime not found"));
    }

    @Override
    @Transactional
    public Showtime createShowtime(CreateShowtimeRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found"));
        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Screen not found"));

        LocalTime newStartTime = request.getStartTime();
        LocalTime newEndTime = newStartTime.plusMinutes(movie.getDuration());

        checkShowtimeOverlap(screen.getId(), request.getShowDate(), newStartTime, newEndTime, -1);

        Showtime showtime = Showtime.builder()
                .movie(movie)
                .screen(screen)
                .showDate(request.getShowDate())
                .startTime(newStartTime)
                .endTime(newEndTime)
                .availableSeats(screen.getTotalSeats())
                .status(Showtime.ShowtimeStatus.scheduled)
                .build();

        List<SeatPrice> seatPrices = request.getSeatPrices().stream()
                .map(priceRequest -> SeatPrice.builder()
                        .showtime(showtime)
                        .seatType(priceRequest.getSeatType())
                        .price(priceRequest.getPrice())
                        .build())
                .collect(Collectors.toList());
        
        showtime.setSeatPrices(seatPrices);

        return showtimeRepository.save(showtime);
    }

    @Override
    @Transactional
    public Showtime updateShowtime(Integer id, UpdateShowtimeRequest request) {
        Showtime showtime = getShowtimeById(id);

        LocalTime newStartTime = request.getStartTime() != null ? request.getStartTime() : showtime.getStartTime();
        LocalDate newShowDate = request.getShowDate() != null ? request.getShowDate() : showtime.getShowDate();
        LocalTime newEndTime = newStartTime.plusMinutes(showtime.getMovie().getDuration());

        checkShowtimeOverlap(showtime.getScreen().getId(), newShowDate, newStartTime, newEndTime, showtime.getId());

        showtime.setShowDate(newShowDate);
        showtime.setStartTime(newStartTime);
        showtime.setEndTime(newEndTime);

        if (request.getStatus() != null) {
            showtime.setStatus(request.getStatus());
        }

        if (request.getSeatPrices() != null && !request.getSeatPrices().isEmpty()) {
            showtime.getSeatPrices().clear();
            List<SeatPrice> newSeatPrices = request.getSeatPrices().stream()
                    .map(priceRequest -> SeatPrice.builder()
                            .showtime(showtime)
                            .seatType(priceRequest.getSeatType())
                            .price(priceRequest.getPrice())
                            .build())
                    .collect(Collectors.toList());
            showtime.getSeatPrices().addAll(newSeatPrices);
        }

        return showtimeRepository.save(showtime);
    }

    @Override
    public void deleteShowtime(Integer id) {
        Showtime showtime = getShowtimeById(id);
        showtime.setStatus(Showtime.ShowtimeStatus.cancelled);
        showtimeRepository.save(showtime);
    }

    @Override
    public List<ShowtimeResponseDTO> getShowtimesDTOByMovie(Integer movieId, LocalDate date) {
        return showtimeRepository.findByMovieIdAndAfterDate(movieId, date).stream()
                .map(ShowtimeResponseDTO::new)
                .collect(Collectors.toList());
    }

    private void checkShowtimeOverlap(Integer screenId, LocalDate showDate, LocalTime newStartTime, LocalTime newEndTime, Integer excludeShowtimeId) {
        List<Showtime> existingShowtimes = showtimeRepository.findOverlappingShowtimes(screenId, showDate, excludeShowtimeId);

        for (Showtime existing : existingShowtimes) {
            if (newStartTime.isBefore(existing.getEndTime()) && newEndTime.isAfter(existing.getStartTime())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Showtime conflicts with an existing showtime from " + existing.getStartTime() + " to " + existing.getEndTime());
            }
        }
    }
}

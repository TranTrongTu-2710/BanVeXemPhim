package com.example.demo.services.serviceIplm;

import com.example.demo.model.SeatPrice;
import com.example.demo.model.Showtime;
import com.example.demo.repository.SeatPriceRepository;
import com.example.demo.repository.ShowtimeRepository;
import com.example.demo.request.seatprice.CreateSeatPriceRequest;
import com.example.demo.request.seatprice.UpdateSeatPriceRequest;
import com.example.demo.services.SeatPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatPriceServiceImpl implements SeatPriceService {

    private final SeatPriceRepository seatPriceRepository;
    private final ShowtimeRepository showtimeRepository;

    @Override
    public SeatPrice createSeatPrice(CreateSeatPriceRequest request) {
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Showtime not found"));

        SeatPrice seatPrice = SeatPrice.builder()
                .showtime(showtime)
                .seatType(request.getSeatType())
                .price(request.getPrice())
                // Đã xóa logic liên quan đến dayType
                .build();
        return seatPriceRepository.save(seatPrice);
    }

    @Override
    public SeatPrice getSeatPriceById(Integer id) {
        return seatPriceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seat price not found"));
    }

    @Override
    public List<SeatPrice> getSeatPricesByShowtime(Integer showtimeId) {
        return seatPriceRepository.findByShowtimeId(showtimeId);
    }

    @Override
    public SeatPrice updateSeatPrice(Integer id, UpdateSeatPriceRequest request) {
        SeatPrice seatPrice = getSeatPriceById(id);
        if (request.getPrice() != null) {
            seatPrice.setPrice(request.getPrice());
        }
        return seatPriceRepository.save(seatPrice);
    }

    @Override
    public void deleteSeatPrice(Integer id) {
        if (!seatPriceRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Seat price not found");
        }
        seatPriceRepository.deleteById(id);
    }
}

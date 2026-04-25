package com.example.demo.services.serviceIplm;

import com.example.demo.model.Screen;
import com.example.demo.model.Seat;
import com.example.demo.repository.ScreenRepository;
import com.example.demo.repository.SeatRepository;
import com.example.demo.request.seat.CreateSeatRequest;
import com.example.demo.request.seat.UpdateSeatRequest;
import com.example.demo.services.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final ScreenRepository screenRepository;

    @Override
    public Seat createSeat(CreateSeatRequest request) {
        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Screen not found"));

        // Kiểm tra xem ghế đã tồn tại chưa (trong cùng 1 phòng chiếu)
        if (seatRepository.existsByScreenIdAndRowNameAndSeatNumber(screen.getId(), request.getRowName(), request.getSeatNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seat already exists in this screen");
        }

        Seat seat = Seat.builder()
                .screen(screen)
                .rowName(request.getRowName())
                .seatNumber(request.getSeatNumber())
                .seatType(request.getSeatType())
                .isActive(true)
                .build();
        return seatRepository.save(seat);
    }

    @Override
    public Seat getSeatById(Integer id) {
        return seatRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seat not found"));
    }

    @Override
    public List<Seat> getSeatsByScreen(Integer screenId) {
        return seatRepository.findByScreenId(screenId);
    }

    @Override
    public Seat updateSeat(Integer id, UpdateSeatRequest request) {
        Seat seat = getSeatById(id);

        if (request.getRowName() != null) seat.setRowName(request.getRowName());
        if (request.getSeatNumber() != null) seat.setSeatNumber(request.getSeatNumber());
        if (request.getSeatType() != null) seat.setSeatType(request.getSeatType());
        if (request.getIsActive() != null) seat.setIsActive(request.getIsActive());

        return seatRepository.save(seat);
    }

    @Override
    public void deleteSeat(Integer id) {
        Seat seat = getSeatById(id);
        seat.setIsActive(false); // Soft delete
        seatRepository.save(seat);
    }
}

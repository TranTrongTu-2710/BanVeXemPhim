package com.example.demo.services.serviceIplm;

import com.example.demo.model.Cinema;
import com.example.demo.model.Screen;
import com.example.demo.repository.CinemaRepository;
import com.example.demo.repository.ScreenRepository;
import com.example.demo.request.screen.CreateScreenRequest;
import com.example.demo.request.screen.UpdateScreenRequest;
import com.example.demo.services.ScreenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScreenServiceImpl implements ScreenService {

    private final ScreenRepository screenRepository;
    private final CinemaRepository cinemaRepository;

    @Override
    public Screen createScreen(CreateScreenRequest request) {
        Cinema cinema = cinemaRepository.findById(request.getCinemaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cinema not found"));

        Screen screen = Screen.builder()
                .cinema(cinema)
                .name(request.getName())
                .screenType(request.getScreenType())
                .totalSeats(request.getTotalSeats())
                .build();
        return screenRepository.save(screen);
    }

    @Override
    public Screen getScreenById(Integer id) {
        return screenRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Screen not found"));
    }

    @Override
    public List<Screen> getAllScreens() {
        return screenRepository.findAll();
    }

    @Override
    public List<Screen> getScreensByCinema(Integer cinemaId) {
        return screenRepository.findByCinemaId(cinemaId);
    }

    @Override
    public Screen updateScreen(Integer id, UpdateScreenRequest request) {
        Screen screen = getScreenById(id);

        if (request.getName() != null) screen.setName(request.getName());
        if (request.getScreenType() != null) screen.setScreenType(request.getScreenType());
        if (request.getTotalSeats() != null) screen.setTotalSeats(request.getTotalSeats());
        if (request.getIsActive() != null) screen.setIsActive(request.getIsActive());

        return screenRepository.save(screen);
    }

    @Override
    public void deleteScreen(Integer id) {
        Screen screen = getScreenById(id);
        screen.setIsActive(false); // Soft delete
        screenRepository.save(screen);
    }
}

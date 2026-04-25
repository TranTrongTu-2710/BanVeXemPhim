package com.example.demo.services.serviceIplm;

import com.example.demo.model.Cinema;
import com.example.demo.repository.CinemaRepository;
import com.example.demo.request.cinema.CreateCinemaRequest;
import com.example.demo.request.cinema.UpdateCinemaRequest;
import com.example.demo.response.CinemaWithShowtimesDTO;
import com.example.demo.services.CinemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CinemaServiceImpl implements CinemaService {

    private final CinemaRepository cinemaRepository;

    @Override
    public List<CinemaWithShowtimesDTO> findCinemasByMovie(Integer movieId) {
        List<Cinema> cinemas = cinemaRepository.findActiveCinemasByMovieAndDate(movieId, LocalDate.now());
        
        return cinemas.stream()
                .map(cinema -> new CinemaWithShowtimesDTO(cinema, movieId))
                .collect(Collectors.toList());
    }

    // --- Các hàm đã có, không thay đổi ---
    @Override
    public Cinema createCinema(CreateCinemaRequest request) {
        Cinema cinema = Cinema.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .district(request.getDistrict())
                .phone(request.getPhone())
                .email(request.getEmail())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .description(request.getDescription())
                .facilities(request.getFacilities())
                .build();
        return cinemaRepository.save(cinema);
    }

    @Override
    public Cinema getCinemaById(Integer id) {
        return cinemaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cinema not found"));
    }

    @Override
    public List<Cinema> getAllCinemas() {
        return cinemaRepository.findAll();
    }

    @Override
    public List<Cinema> getActiveCinemas() {
        return cinemaRepository.findByIsActive(true);
    }

    @Override
    public Cinema updateCinema(Integer id, UpdateCinemaRequest request) {
        Cinema cinema = getCinemaById(id);

        if (request.getName() != null) cinema.setName(request.getName());
        if (request.getAddress() != null) cinema.setAddress(request.getAddress());
        if (request.getCity() != null) cinema.setCity(request.getCity());
        if (request.getDistrict() != null) cinema.setDistrict(request.getDistrict());
        if (request.getPhone() != null) cinema.setPhone(request.getPhone());
        if (request.getEmail() != null) cinema.setEmail(request.getEmail());
        if (request.getLatitude() != null) cinema.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) cinema.setLongitude(request.getLongitude());
        if (request.getDescription() != null) cinema.setDescription(request.getDescription());
        if (request.getFacilities() != null) cinema.setFacilities(request.getFacilities());
        if (request.getIsActive() != null) cinema.setIsActive(request.getIsActive());

        return cinemaRepository.save(cinema);
    }

    @Override
    public void deleteCinema(Integer id) {
        Cinema cinema = getCinemaById(id);
        cinema.setIsActive(false);
        cinemaRepository.save(cinema);
    }
}

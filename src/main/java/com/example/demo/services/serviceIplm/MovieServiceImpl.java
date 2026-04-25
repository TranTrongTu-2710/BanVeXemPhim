package com.example.demo.services.serviceIplm;

import com.example.demo.model.Movie;
import com.example.demo.model.User;
import com.example.demo.repository.MovieRepository;
import com.example.demo.request.movie.CreateMovieRequest;
import com.example.demo.request.movie.UpdateMovieRequest;
import com.example.demo.response.MovieResponseDTO;
import com.example.demo.services.FileStorageService;
import com.example.demo.services.MovieService;
import com.example.demo.services.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final SecurityService securityService;
    private final FileStorageService fileStorageService;

    @Value("${server.public-url:http://localhost:8080}")
    private String serverUrl;

    @Override
    @CacheEvict(value = "movies", allEntries = true)
    public Movie createMovie(CreateMovieRequest request) {
        String posterUrlString = null;
        // SỬA: Dùng getPosterUrl() (trả về MultipartFile)
        if (request.getPosterUrl() != null && !request.getPosterUrl().isEmpty()) {
            String fileName = fileStorageService.storeFile(request.getPosterUrl());
            posterUrlString = serverUrl + "/uploads/" + fileName;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Poster image is required");
        }

        Movie movie = Movie.builder()
                .title(request.getTitle())
                .originalTitle(request.getOriginalTitle())
                .description(request.getDescription())
                .duration(request.getDuration())
                .releaseDate(request.getReleaseDate())
                .endDate(request.getEndDate())
                .director(request.getDirector())
                .cast(request.getCast())
                .genres(request.getGenres())
                .language(request.getLanguage())
                .subtitle(request.getSubtitle())
                .rating(request.getRating())
                .posterUrl(posterUrlString) // Lưu chuỗi URL vào DB
                .trailerUrl(request.getTrailerUrl())
                .bannerUrl(request.getBannerUrl())
                .country(request.getCountry())
                .status(request.getStatus() != null ? request.getStatus() : Movie.MovieStatus.coming_soon)
                .build();
        return movieRepository.save(movie);
    }

    @Override
    @CacheEvict(value = "movies", allEntries = true)
    public Movie updateMovie(Integer id, UpdateMovieRequest request) {
        Movie movie = getMovieByIdForAdmin(id);
        
        String currentPosterUrl = movie.getPosterUrl();
        // SỬA: Dùng getPosterUrl()
        if (request.getPosterUrl() != null && !request.getPosterUrl().isEmpty()) {
            // Xóa ảnh cũ
            if (currentPosterUrl != null && currentPosterUrl.contains("/uploads/")) {
                String oldFileName = currentPosterUrl.substring(currentPosterUrl.lastIndexOf("/") + 1);
                fileStorageService.deleteFile(oldFileName);
            }
            // Lưu ảnh mới
            String fileName = fileStorageService.storeFile(request.getPosterUrl());
            movie.setPosterUrl(serverUrl + "/uploads/" + fileName);
        }

        if (request.getTitle() != null) movie.setTitle(request.getTitle());
        if (request.getOriginalTitle() != null) movie.setOriginalTitle(request.getOriginalTitle());
        if (request.getDescription() != null) movie.setDescription(request.getDescription());
        if (request.getDuration() != null) movie.setDuration(request.getDuration());
        if (request.getReleaseDate() != null) movie.setReleaseDate(request.getReleaseDate());
        if (request.getEndDate() != null) movie.setEndDate(request.getEndDate());
        if (request.getDirector() != null) movie.setDirector(request.getDirector());
        if (request.getCast() != null) movie.setCast(request.getCast());
        if (request.getGenres() != null) movie.setGenres(request.getGenres());
        if (request.getLanguage() != null) movie.setLanguage(request.getLanguage());
        if (request.getSubtitle() != null) movie.setSubtitle(request.getSubtitle());
        if (request.getRating() != null) movie.setRating(request.getRating());
        if (request.getTrailerUrl() != null) movie.setTrailerUrl(request.getTrailerUrl());
        if (request.getBannerUrl() != null) movie.setBannerUrl(request.getBannerUrl());
        if (request.getCountry() != null) movie.setCountry(request.getCountry());
        if (request.getStatus() != null) movie.setStatus(request.getStatus());
        if (request.getIsActive() != null) movie.setIsActive(request.getIsActive());
        
        return movieRepository.save(movie);
    }

    @Override
    @CacheEvict(value = "movies", allEntries = true)
    public void deleteMovie(Integer id) {
        Movie movie = getMovieByIdForAdmin(id);
        
        String posterUrl = movie.getPosterUrl();
        if (posterUrl != null && posterUrl.contains("/uploads/")) {
            String fileName = posterUrl.substring(posterUrl.lastIndexOf("/") + 1);
            fileStorageService.deleteFile(fileName);
        }
        
        movieRepository.delete(movie);
    }

    @Override
    public Movie getMovieByIdForAdmin(Integer id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found"));
    }

    @Override
    public List<Movie> getAllMoviesForAdmin() {
        return movieRepository.findAll();
    }

    @Override
    public MovieResponseDTO getActiveMovieByIdDTO(Integer id) {
        Movie movie = movieRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found or is not active"));
        return new MovieResponseDTO(movie);
    }

    @Cacheable("movies")
    @Override
    public List<MovieResponseDTO> getAllActiveMoviesDTO() {
        return movieRepository.findByIsActiveTrue().stream()
                .map(MovieResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovieResponseDTO> getActiveMoviesByStatusDTO(Movie.MovieStatus status) {
        return movieRepository.findByStatusAndIsActiveTrue(status).stream()
                .map(MovieResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovieResponseDTO> getScheduledMoviesForStaffCinemaTodayDTO() {
        User staff = securityService.getCurrentUser();
        LocalDate today = LocalDate.now();

        if (staff.getRole() == User.Role.admin) {
            return movieRepository.findAll().stream()
                    .filter(movie -> movie.getShowtimes().stream()
                            .anyMatch(showtime -> showtime.getShowDate().isEqual(today)))
                    .map(MovieResponseDTO::new)
                    .collect(Collectors.toList());
        }

        if (staff.getRole() == User.Role.staff && staff.getCinema() != null) {
            Integer cinemaId = staff.getCinema().getId();
            return movieRepository.findScheduledMoviesByCinemaAndDate(cinemaId, today).stream()
                    .map(MovieResponseDTO::new)
                    .collect(Collectors.toList());
        }
        
        return Collections.emptyList();
    }
}

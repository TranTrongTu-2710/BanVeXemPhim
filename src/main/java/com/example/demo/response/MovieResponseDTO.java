package com.example.demo.response;

import com.example.demo.model.Movie;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class MovieResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String title;
    private String originalTitle;
    private String description;
    private Integer duration;
    private LocalDate releaseDate;
    private LocalDate endDate;
    private String director;
    private String cast;
    private String genres;
    private String language;
    private String subtitle;
    private Movie.MovieRating rating;
    private String posterUrl;
    private String trailerUrl;
    private String bannerUrl;
    private String country;
    private Movie.MovieStatus status;
    private List<ShowtimeResponseDTO> showtimes;

    public MovieResponseDTO(Movie movie) {
        this.id = movie.getId();
        this.title = movie.getTitle();
        this.originalTitle = movie.getOriginalTitle();
        this.description = movie.getDescription();
        this.duration = movie.getDuration();
        this.releaseDate = movie.getReleaseDate();
        this.endDate = movie.getEndDate();
        this.director = movie.getDirector();
        this.cast = movie.getCast();
        this.genres = movie.getGenres();
        this.language = movie.getLanguage();
        this.subtitle = movie.getSubtitle();
        this.rating = movie.getRating();
        this.posterUrl = movie.getPosterUrl();
        this.trailerUrl = movie.getTrailerUrl();
        this.bannerUrl = movie.getBannerUrl();
        this.country = movie.getCountry();
        this.status = movie.getStatus();
        
        // **LOGIC LỌC MỚI**
        // Chuyển đổi và chỉ lấy các suất chiếu từ hôm nay trở đi
        if (movie.getShowtimes() != null) {
            LocalDate today = LocalDate.now();
            this.showtimes = movie.getShowtimes().stream()
                    .filter(showtime -> !showtime.getShowDate().isBefore(today)) // Lọc các suất chiếu chưa hết hạn
                    .map(ShowtimeResponseDTO::new)
                    .collect(Collectors.toList());
        }
    }
}

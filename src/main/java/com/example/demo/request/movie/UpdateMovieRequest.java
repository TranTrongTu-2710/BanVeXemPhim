package com.example.demo.request.movie;

import com.example.demo.model.Movie;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class UpdateMovieRequest {
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
    
    // SỬA: Đổi tên thành posterUrl
    private MultipartFile posterUrl;

    private String trailerUrl;
    private String bannerUrl;
    private String country;
    private Movie.MovieStatus status;
    private Boolean isActive;
}

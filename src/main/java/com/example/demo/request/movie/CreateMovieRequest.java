package com.example.demo.request.movie;

import com.example.demo.model.Movie;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class CreateMovieRequest {
    @NotBlank
    private String title;
    private String originalTitle;
    private String description;
    @NotNull
    private Integer duration;
    @NotNull
    private LocalDate releaseDate;
    private LocalDate endDate;
    private String director;
    private String cast;
    private String genres;
    private String language;
    private String subtitle;
    @NotNull
    private Movie.MovieRating rating;
    
    // SỬA: Đổi tên thành posterUrl để khớp với key gửi từ Postman
    private MultipartFile posterUrl;
    
    private String trailerUrl;
    private String bannerUrl;
    private String country;
    private Movie.MovieStatus status;
}

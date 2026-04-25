package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Movie implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(name = "original_title")
    private String originalTitle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Column(nullable = false)
    private Integer duration; // in minutes

    @NotNull
    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    private String director;

    @Column(columnDefinition = "TEXT")
    private String cast; // JSON array of actors

    @Column(columnDefinition = "TEXT")
    private String genres; // JSON array of genres

    private String language;
    private String subtitle;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovieRating rating;

    @Column(name = "poster_url", length = 500)
    private String posterUrl;

    @Column(name = "trailer_url", length = 500)
    private String trailerUrl;

    @Column(name = "banner_url", length = 500)
    private String bannerUrl;

    private String country;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private MovieStatus status = MovieStatus.coming_soon;

    @Builder.Default
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("movie-showtime")
    private List<Showtime> showtimes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("movie-review")
    private List<Review> reviews = new ArrayList<>();

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum MovieRating {
        P, C13, C16, C18
    }

    public enum MovieStatus {
        coming_soon, now_showing, ended
    }
}

package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "showtimes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"screen_id", "show_date", "start_time"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Showtime implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    @JsonBackReference("movie-showtime")
    private Movie movie;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    @JsonBackReference("screen-showtime")
    private Screen screen;

    @NotNull
    @Column(name = "show_date", nullable = false)
    private LocalDate showDate;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    // basePrice đã được xóa

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShowtimeStatus status = ShowtimeStatus.scheduled;

    @NotNull
    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Builder.Default
    @OneToMany(mappedBy = "showtime", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER) // EAGER fetch để dễ lấy giá
    @JsonManagedReference
    private List<SeatPrice> seatPrices = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ShowtimeStatus {
        scheduled, selling, sold_out, cancelled
    }
}

package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Entity
@Table(name = "screens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Screen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_id", nullable = false)
    @JsonBackReference // To prevent infinite recursion with Cinema
    private Cinema cinema;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "screen_type", nullable = false)
    private ScreenType screenType;

    @NotNull
    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Builder.Default
    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("screen-seat")
    private List<Seat> seats = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("screen-showtime")
    private List<Showtime> showtimes = new ArrayList<>();

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ScreenType {
        IMAX, FULL_HD;
    }
}

package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "seats", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"screen_id", "row_name", "seat_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    @JsonBackReference
    private Screen screen;

    @NotBlank
    @Column(name = "row_name", nullable = false, length = 5)
    private String rowName;

    @NotNull
    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false)
    private SeatType seatType;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum SeatType {
        standard, vip, couple
    }
}

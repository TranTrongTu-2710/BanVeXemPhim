package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "seat_prices", uniqueConstraints = {
    // Ràng buộc duy nhất giờ chỉ cần showtime và loại ghế
    @UniqueConstraint(columnNames = {"showtime_id", "seat_type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    @JsonBackReference
    private Showtime showtime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false)
    private Seat.SeatType seatType;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Đã xóa trường dayType và enum DayType
}

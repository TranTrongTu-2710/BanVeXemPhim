package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_food")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingFood {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonBackReference("booking-food")
    private Booking booking;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_item_id", nullable = false)
    private FoodItem foodItem;

    @NotNull
    @Column(nullable = false)
    private Integer quantity;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // Price at the time of booking

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

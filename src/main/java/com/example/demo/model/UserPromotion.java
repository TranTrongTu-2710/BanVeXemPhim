package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPromotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

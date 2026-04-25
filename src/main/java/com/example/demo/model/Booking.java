package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-booking")
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @NotBlank
    @Column(name = "booking_code", unique = true, nullable = false, length = 20)
    private String bookingCode;

    @NotNull
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Builder.Default
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "final_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalAmount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.pending;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false)
    private BookingStatus bookingStatus = BookingStatus.pending;

    @Builder.Default
    @Column(name = "points_earned")
    private Integer pointsEarned = 0;

    @Builder.Default
    @Column(name = "points_used")
    private Integer pointsUsed = 0;

    @CreationTimestamp
    @Column(name = "booking_date", updatable = false)
    private LocalDateTime bookingDate;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "qr_code", length = 500)
    private String qrCode;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("booking-seat")
    private List<BookingSeat> bookingSeats = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("booking-food")
    private List<BookingFood> bookingFoods = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("booking-review")
    private List<Review> reviews = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("booking-payment")
    private List<PaymentTransaction> paymentTransactions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enum definitions
    public enum PaymentMethod {
        cash, momo, zalopay, vnpay, credit_card
    }

    public enum PaymentStatus {
        pending, paid, failed, refunded
    }

    public enum BookingStatus {
        pending, confirmed, cancelled, completed
    }
}

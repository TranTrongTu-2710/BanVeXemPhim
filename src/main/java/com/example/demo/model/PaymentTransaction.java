package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonBackReference("booking-payment")
    private Booking booking;

    @NotBlank
    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private Booking.PaymentMethod paymentMethod;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "payment_data", columnDefinition = "TEXT")
    private String paymentData;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Status {
        pending, success, failed, refunded
    }
}

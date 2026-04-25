package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "food_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Thay thế user_id bằng staff_id
    @NotNull // Nhân viên bán hàng là bắt buộc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private User staff;

    @NotNull
    @Column(name = "final_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalAmount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private Booking.PaymentMethod paymentMethod;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.COMPLETED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @OneToMany(mappedBy = "foodOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<FoodOrderItem> orderItems = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum OrderStatus {
        PENDING, COMPLETED, CANCELLED
    }
}

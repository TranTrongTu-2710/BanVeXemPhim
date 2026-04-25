package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @NotBlank
    @Column(nullable = false)
    private String name;

    // SỬA Ở ĐÂY: Dùng LONGTEXT để lưu nội dung dài như bài báo HTML
    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;

    @NotNull
    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "min_purchase_amount", precision = 10, scale = 2)
    private BigDecimal minPurchaseAmount;

    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Builder.Default
    @Column(name = "used_count")
    private Integer usedCount = 0;

    @NotNull
    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @NotNull
    @Column(name = "valid_to", nullable = false)
    private LocalDateTime validTo;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "applicable_to")
    private ApplicableTo applicableTo = ApplicableTo.all;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "area_apply")
    private AreaApply areaApply = AreaApply.PL;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "rank_user_apply")
    private RankUserApply rankUserApply = RankUserApply.ALL;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum DiscountType {
        percentage, fixed_amount, points
    }

    public enum ApplicableTo {
        all, movie, food, booking
    }

    public enum AreaApply {
        PL, PV
    }

    public enum RankUserApply {
        ALL, GOLD, PLATINUM
    }
}

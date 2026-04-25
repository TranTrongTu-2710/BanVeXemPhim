package com.example.demo.request.promotion;

import com.example.demo.model.Promotion;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreatePromotionRequest {
    @NotBlank
    private String code;
    @NotBlank
    private String name;
    private String description;
    @NotNull
    private Promotion.DiscountType discountType;
    @NotNull
    private BigDecimal discountValue;
    private BigDecimal minPurchaseAmount;
    private BigDecimal maxDiscountAmount;
    private Integer usageLimit;
    @NotNull
    private LocalDateTime validFrom;
    @NotNull
    private LocalDateTime validTo;
    private Promotion.ApplicableTo applicableTo;
    private Promotion.AreaApply areaApply; // Thêm trường mới
    private Promotion.RankUserApply rankUserApply; // Thêm trường mới
}
package com.example.demo.request.promotion;

import com.example.demo.model.Promotion;
import jakarta.validation.constraints.Future;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdatePromotionRequest {
    private String name;
    private String description;
    private Promotion.DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minPurchaseAmount;
    private BigDecimal maxDiscountAmount;
    private Integer usageLimit;
    private LocalDateTime validFrom;
    @Future
    private LocalDateTime validTo;
    private Promotion.ApplicableTo applicableTo;
    private Promotion.AreaApply areaApply; // Thêm trường mới
    private Promotion.RankUserApply rankUserApply; // Thêm trường mới
    private Boolean isActive;
}
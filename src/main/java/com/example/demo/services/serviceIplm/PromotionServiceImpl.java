package com.example.demo.services.serviceIplm;

import com.example.demo.model.Promotion;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.request.promotion.CreatePromotionRequest;
import com.example.demo.request.promotion.UpdatePromotionRequest;
import com.example.demo.services.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {
    @Override
    public List<Promotion> getActivePublicPromotions() {
        return promotionRepository.findByIsActiveAndValidToAfter(true, LocalDateTime.now());
    }

    private final PromotionRepository promotionRepository;

    @Override
    public Promotion createPromotion(CreatePromotionRequest request) {
        if (promotionRepository.findByCode(request.getCode()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Promotion code already exists.");
        }
        Promotion promotion = Promotion.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minPurchaseAmount(request.getMinPurchaseAmount())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .usageLimit(request.getUsageLimit())
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .applicableTo(request.getApplicableTo())
                .areaApply(request.getAreaApply()) // Thêm logic
                .rankUserApply(request.getRankUserApply()) // Thêm logic
                .build();
        return promotionRepository.save(promotion);
    }

    @Override
    public Promotion getPromotionById(Integer id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Promotion not found"));
    }

    @Override
    public Promotion getPromotionByCode(String code) {
        return promotionRepository.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Promotion not found"));
    }

    @Override
    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    @Override
    public Promotion updatePromotion(Integer id, UpdatePromotionRequest request) {
        Promotion promotion = getPromotionById(id);

        if (request.getName() != null) promotion.setName(request.getName());
        if (request.getDescription() != null) promotion.setDescription(request.getDescription());
        if (request.getDiscountType() != null) promotion.setDiscountType(request.getDiscountType());
        if (request.getDiscountValue() != null) promotion.setDiscountValue(request.getDiscountValue());
        if (request.getMinPurchaseAmount() != null) promotion.setMinPurchaseAmount(request.getMinPurchaseAmount());
        if (request.getMaxDiscountAmount() != null) promotion.setMaxDiscountAmount(request.getMaxDiscountAmount());
        if (request.getUsageLimit() != null) promotion.setUsageLimit(request.getUsageLimit());
        if (request.getValidFrom() != null) promotion.setValidFrom(request.getValidFrom());
        if (request.getValidTo() != null) promotion.setValidTo(request.getValidTo());
        if (request.getApplicableTo() != null) promotion.setApplicableTo(request.getApplicableTo());
        if (request.getAreaApply() != null) promotion.setAreaApply(request.getAreaApply()); // Thêm logic
        if (request.getRankUserApply() != null) promotion.setRankUserApply(request.getRankUserApply()); // Thêm logic
        if (request.getIsActive() != null) promotion.setIsActive(request.getIsActive());

        return promotionRepository.save(promotion);
    }

    @Override
    public void deletePromotion(Integer id) {
        Promotion promotion = getPromotionById(id);
        promotion.setIsActive(false);
        promotionRepository.save(promotion);
    }
}
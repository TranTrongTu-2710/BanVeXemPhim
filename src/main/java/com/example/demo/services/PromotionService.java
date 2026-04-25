package com.example.demo.services;

import com.example.demo.model.Promotion;
import com.example.demo.request.promotion.CreatePromotionRequest;
import com.example.demo.request.promotion.UpdatePromotionRequest;

import java.util.List;

public interface PromotionService {
    Promotion createPromotion(CreatePromotionRequest request);
    Promotion getPromotionById(Integer id);
    Promotion getPromotionByCode(String code);
    List<Promotion> getAllPromotions(); // For Admin
    List<Promotion> getActivePublicPromotions(); // For Customer
    Promotion updatePromotion(Integer id, UpdatePromotionRequest request);
    void deletePromotion(Integer id);
}

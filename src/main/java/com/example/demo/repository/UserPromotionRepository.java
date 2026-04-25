package com.example.demo.repository;

import com.example.demo.model.UserPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPromotionRepository extends JpaRepository<UserPromotion, Integer> {
}

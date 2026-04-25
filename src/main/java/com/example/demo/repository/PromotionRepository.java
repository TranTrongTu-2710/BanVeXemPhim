package com.example.demo.repository;

import com.example.demo.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    Optional<Promotion> findByCode(String code);

    // Phương thức cũ, có thể giữ lại hoặc xóa nếu không dùng
    List<Promotion> findByIsActiveAndValidToAfter(boolean isActive, LocalDateTime now);

    // Phương thức mới để lấy KM công khai
    List<Promotion> findByIsActiveAndAreaApplyAndValidToAfter(boolean isActive, Promotion.AreaApply areaApply, LocalDateTime now);
}
package com.example.demo.repository;

import com.example.demo.model.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Integer> {
    // Tìm tất cả banner đang hoạt động để hiển thị cho user
    List<Banner> findByIsActiveTrue();
}

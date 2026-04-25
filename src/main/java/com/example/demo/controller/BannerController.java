package com.example.demo.controller;

import com.example.demo.model.Banner;
import com.example.demo.request.CreateBannerRequest;
import com.example.demo.services.BannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    // Public: Lấy danh sách banner đang hoạt động (cho trang chủ)
    @GetMapping
    public ResponseEntity<List<Banner>> getActiveBanners() {
        return ResponseEntity.ok(bannerService.getActiveBanners());
    }

    // Admin: Lấy tất cả banner (bao gồm cả ẩn)
    @GetMapping("/admin/all")
    public ResponseEntity<List<Banner>> getAllBanners() {
        return ResponseEntity.ok(bannerService.getAllBanners());
    }

    // Admin: Lấy chi tiết banner
    @GetMapping("/{id}")
    public ResponseEntity<Banner> getBannerById(@PathVariable Integer id) {
        return ResponseEntity.ok(bannerService.getBannerById(id));
    }

    // Admin: Tạo mới banner (Sử dụng @ModelAttribute để nhận file upload)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Banner> createBanner(@Valid @ModelAttribute CreateBannerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bannerService.createBanner(request));
    }

    // Admin: Cập nhật banner (Sử dụng @ModelAttribute để nhận file upload)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Banner> updateBanner(@PathVariable Integer id, @Valid @ModelAttribute CreateBannerRequest request) {
        return ResponseEntity.ok(bannerService.updateBanner(id, request));
    }

    // Admin: Xóa banner
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteBanner(@PathVariable Integer id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.ok(Map.of("message", "Banner deleted successfully"));
    }
}

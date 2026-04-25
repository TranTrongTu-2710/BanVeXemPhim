package com.example.demo.services;

import com.example.demo.model.Banner;
import com.example.demo.request.CreateBannerRequest;

import java.util.List;

public interface BannerService {
    List<Banner> getAllBanners(); // Cho Admin (thấy cả ẩn/hiện)
    List<Banner> getActiveBanners(); // Cho User (chỉ thấy hiện)
    Banner getBannerById(Integer id);
    Banner createBanner(CreateBannerRequest request);
    Banner updateBanner(Integer id, CreateBannerRequest request);
    void deleteBanner(Integer id);
}

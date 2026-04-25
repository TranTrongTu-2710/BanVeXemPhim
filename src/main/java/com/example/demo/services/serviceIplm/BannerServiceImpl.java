package com.example.demo.services.serviceIplm;

import com.example.demo.model.Banner;
import com.example.demo.repository.BannerRepository;
import com.example.demo.request.CreateBannerRequest;
import com.example.demo.services.BannerService;
import com.example.demo.services.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;
    private final FileStorageService fileStorageService;

    // Thêm giá trị mặc định để tránh lỗi nếu quên cấu hình
    @Value("${server.public-url:http://localhost:8080}") 
    private String serverUrl;

    @Override
    public List<Banner> getAllBanners() {
        return bannerRepository.findAll();
    }

    @Override
    public List<Banner> getActiveBanners() {
        return bannerRepository.findByIsActiveTrue();
    }

    @Override
    public Banner getBannerById(Integer id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Banner not found"));
    }

    @Override
    public Banner createBanner(CreateBannerRequest request) {
        if (request.getImage() == null || request.getImage().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required");
        }

        String fileName = fileStorageService.storeFile(request.getImage());
        // Lưu đường dẫn đầy đủ để frontend dễ hiển thị
        String fileUrl = serverUrl + "/uploads/" + fileName;

        Banner banner = Banner.builder()
                .name(request.getName())
                .imageUrl(fileUrl)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        return bannerRepository.save(banner);
    }

    @Override
    public Banner updateBanner(Integer id, CreateBannerRequest request) {
        Banner banner = getBannerById(id);
        
        banner.setName(request.getName());
        
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            // Xóa ảnh cũ nếu có
            String oldUrl = banner.getImageUrl();
            if (oldUrl != null && oldUrl.contains("/uploads/")) {
                String oldFileName = oldUrl.substring(oldUrl.lastIndexOf("/") + 1);
                fileStorageService.deleteFile(oldFileName);
            }

            // Lưu ảnh mới
            String fileName = fileStorageService.storeFile(request.getImage());
            String fileUrl = serverUrl + "/uploads/" + fileName;
            banner.setImageUrl(fileUrl);
        }
        
        if (request.getIsActive() != null) {
            banner.setIsActive(request.getIsActive());
        }
        
        return bannerRepository.save(banner);
    }

    @Override
    public void deleteBanner(Integer id) {
        Banner banner = getBannerById(id);
        
        // Xóa file ảnh
        String oldUrl = banner.getImageUrl();
        if (oldUrl != null && oldUrl.contains("/uploads/")) {
            String oldFileName = oldUrl.substring(oldUrl.lastIndexOf("/") + 1);
            fileStorageService.deleteFile(oldFileName);
        }

        bannerRepository.delete(banner);
    }
}

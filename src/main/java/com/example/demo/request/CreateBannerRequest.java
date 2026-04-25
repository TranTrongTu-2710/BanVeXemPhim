package com.example.demo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateBannerRequest {
    @NotBlank(message = "Tên banner không được để trống")
    private String name;

    // Không bắt buộc (để dùng chung cho cả update), sẽ kiểm tra null trong service
    private MultipartFile image;
    
    private Boolean isActive;
}

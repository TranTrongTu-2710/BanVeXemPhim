package com.example.demo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBroadcastNotificationRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    private String message; // Chứa HTML bài báo

    // Có thể thêm loại thông báo nếu cần, mặc định là SYSTEM hoặc PROMOTION
    private String type; 
}

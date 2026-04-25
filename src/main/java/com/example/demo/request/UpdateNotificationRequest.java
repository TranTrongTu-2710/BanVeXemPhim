package com.example.demo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateNotificationRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    private String message;
}

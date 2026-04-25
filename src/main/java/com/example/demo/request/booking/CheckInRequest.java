package com.example.demo.request.booking;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckInRequest {
    @NotBlank(message = "Booking code is required")
    private String bookingCode;
}

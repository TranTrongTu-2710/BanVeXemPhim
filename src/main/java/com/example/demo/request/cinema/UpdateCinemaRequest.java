package com.example.demo.request.cinema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateCinemaRequest {
    @Size(max = 255)
    private String name;

    private String address;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String district;

    @Size(max = 20)
    private String phone;

    @Email
    @Size(max = 255)
    private String email;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;
    private String facilities;
    private Boolean isActive;
}

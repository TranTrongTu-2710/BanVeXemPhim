package com.example.demo.request.user;

import com.example.demo.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateUserRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phone;
    private LocalDate dateOfBirth;
    private User.Gender gender;

    @NotNull(message = "Role is required")
    private User.Role role;
    
    private Integer cinemaId; // Dành cho STAFF, để biết nhân viên thuộc rạp nào
}

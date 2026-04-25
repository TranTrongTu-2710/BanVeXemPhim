package com.example.demo.response;

import com.example.demo.model.User;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserResponseDTO {
    private Integer id;
    private String email;
    private String fullName;
    private String phone;
    private LocalDate dateOfBirth;
    private User.Gender gender;
    private String avatarUrl;
    private User.Role role;
    private Integer points;
    private User.MembershipTier membershipTier;
    private Integer cinemaId; // Chỉ trả về ID của cinema

    // Constructor để chuyển đổi từ User model sang DTO
    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.phone = user.getPhone();
        this.dateOfBirth = user.getDateOfBirth();
        this.gender = user.getGender();
        this.avatarUrl = user.getAvatarUrl();
        this.role = user.getRole();
        this.points = user.getPoints();
        this.membershipTier = user.getMembershipTier();
        
        // Lấy cinemaId một cách an toàn, tránh lỗi NullPointerException
        if (user.getCinema() != null) {
            this.cinemaId = user.getCinema().getId();
        }
    }
}

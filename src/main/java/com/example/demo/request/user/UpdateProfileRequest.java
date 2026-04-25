package com.example.demo.request.user;

import com.example.demo.model.User;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String phone;
    private LocalDate dateOfBirth;
    private User.Gender gender;
    private String avatarUrl;

    public void applyTo(User user) {
        if (fullName != null) {
            user.setFullName(fullName);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (dateOfBirth != null) {
            user.setDateOfBirth(dateOfBirth);
        }
        if (gender != null) {
            user.setGender(gender);
        }
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }
    }
}

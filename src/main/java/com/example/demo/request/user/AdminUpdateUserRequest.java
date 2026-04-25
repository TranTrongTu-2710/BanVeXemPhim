package com.example.demo.request.user;

import com.example.demo.model.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminUpdateUserRequest extends UpdateProfileRequest {
    private User.Role role;
    private Integer points;
    private User.MembershipTier membershipTier;
    private Boolean isActive;
    private Integer cinemaId;
    
    // Thêm trường password để Admin có thể reset mật khẩu cho user
    private String password;
}

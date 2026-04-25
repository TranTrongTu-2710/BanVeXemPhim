package com.example.demo.services;

import com.example.demo.model.User;
import com.example.demo.request.user.AdminUpdateUserRequest;
import com.example.demo.request.user.CreateUserRequest;
import com.example.demo.request.user.RegisterRequest;
import com.example.demo.request.user.UpdateProfileRequest;
import com.example.demo.response.UserResponseDTO;

import java.util.List;
import java.util.Map;

public interface UserService {
    // Auth
    Map<String, Object> registerUser(RegisterRequest request);
    Map<String, Object> login(String email, String password);
    void logout(User user, String token);
    
    // Getters
    UserResponseDTO getCurrentUserDTO();
    UserResponseDTO getUserByIdDTO(Integer id);
    List<UserResponseDTO> getAllUsersDTO();

    // Internal
    User getCurrentUser();
    User getUserById(Integer id);

    // Update
    UserResponseDTO updateUserProfile(UpdateProfileRequest request);
    
    // Admin functions
    UserResponseDTO createUser(CreateUserRequest request); // Admin tạo user
    UserResponseDTO adminUpdateUser(Integer id, AdminUpdateUserRequest request); // Admin sửa user
    void deleteUser(Integer id); // Admin xóa user
}

package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.request.user.AdminUpdateUserRequest;
import com.example.demo.request.user.CreateUserRequest;
import com.example.demo.request.user.LoginRequest;
import com.example.demo.request.user.RegisterRequest;
import com.example.demo.request.user.UpdateProfileRequest;
import com.example.demo.response.UserResponseDTO;
import com.example.demo.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> registerUser(@Valid @RequestBody RegisterRequest request) {
        return new ResponseEntity<>(userService.registerUser(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request.getEmail(), request.getPassword()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        String currentToken = (String) request.getAttribute("currentToken");
        userService.logout(currentUser, currentToken);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMyProfile() {
        return ResponseEntity.ok(userService.getCurrentUserDTO());
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponseDTO> updateMyProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateUserProfile(request));
    }

    // --- Admin & Staff Routes ---

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsersDTO());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getUserByIdDTO(id));
    }

    // --- Admin Only Routes ---

    @PostMapping("/admin")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUserById(@PathVariable Integer id, @Valid @RequestBody AdminUpdateUserRequest request) {
        return ResponseEntity.ok(userService.adminUpdateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUserById(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deactivated successfully."));
    }
}

package com.example.demo.services.serviceIplm;

import com.example.demo.config.SecurityConfig.JwtService;
import com.example.demo.model.Cinema;
import com.example.demo.model.User;
import com.example.demo.repository.CinemaRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.request.user.AdminUpdateUserRequest;
import com.example.demo.request.user.CreateUserRequest;
import com.example.demo.request.user.RegisterRequest;
import com.example.demo.request.user.UpdateProfileRequest;
import com.example.demo.response.UserResponseDTO;
import com.example.demo.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CinemaRepository cinemaRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Map<String, Object> registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already in use.");
        }
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(User.Role.customer)
                .build();
        User savedUser = userRepository.save(user);
        String token = jwtService.sign(savedUser.getId());
        savedUser.getTokens().add(token);
        userRepository.save(savedUser);
        
        UserResponseDTO userResponse = new UserResponseDTO(savedUser);
        return Map.of("user", userResponse, "token", token);
    }

    @Override
    @Transactional
    public Map<String, Object> login(String email, String password) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }
        if (!user.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is deactivated.");
        }

        String token = jwtService.sign(user.getId());
        user.getTokens().add(token);
        userRepository.save(user);

        UserResponseDTO userResponse = new UserResponseDTO(user);
        return Map.of("user", userResponse, "token", token);
    }

    @Override
    @Transactional
    public void logout(User user, String token) {
        user.getTokens().remove(token);
        userRepository.save(user);
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated.");
        }
        return (User) authentication.getPrincipal();
    }

    @Override
    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Override
    public UserResponseDTO getCurrentUserDTO() {
        return new UserResponseDTO(getCurrentUser());
    }

    @Override
    public UserResponseDTO getUserByIdDTO(Integer id) {
        return new UserResponseDTO(getUserById(id));
    }

    @Override
    public List<UserResponseDTO> getAllUsersDTO() {
        return userRepository.findAll().stream().map(UserResponseDTO::new).collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO updateUserProfile(UpdateProfileRequest request) {
        User currentUser = getCurrentUser();
        if (request.getFullName() != null) currentUser.setFullName(request.getFullName());
        if (request.getPhone() != null) currentUser.setPhone(request.getPhone());
        if (request.getDateOfBirth() != null) currentUser.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) currentUser.setGender(request.getGender());
        
        // Kiểm tra nếu URL avatar mới khác URL hiện tại thì mới cập nhật
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().equals(currentUser.getAvatarUrl())) {
            currentUser.setAvatarUrl(request.getAvatarUrl());
        }
        
        User updatedUser = userRepository.save(currentUser);
        return new UserResponseDTO(updatedUser);
    }

    @Override
    public UserResponseDTO createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already in use.");
        }
        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .role(request.getRole())
                .isActive(true)
                .build();

        if (request.getCinemaId() != null) {
            if (request.getRole() != User.Role.staff) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can only assign a cinema to a STAFF user.");
            }
            Cinema cinema = cinemaRepository.findById(request.getCinemaId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cinema not found."));
            user.setCinema(cinema);
        }

        User savedUser = userRepository.save(user);
        return new UserResponseDTO(savedUser);
    }

    @Override
    public UserResponseDTO adminUpdateUser(Integer id, AdminUpdateUserRequest request) {
        User user = getUserById(id);
        
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getDateOfBirth() != null) user.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) user.setGender(request.getGender());
        
        // Kiểm tra nếu URL avatar mới khác URL hiện tại thì mới cập nhật
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().equals(user.getAvatarUrl())) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getPoints() != null) user.setPoints(request.getPoints());
        if (request.getMembershipTier() != null) user.setMembershipTier(request.getMembershipTier());
        if (request.getIsActive() != null) user.setIsActive(request.getIsActive());

        // Cập nhật mật khẩu nếu có
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getCinemaId() != null) {
            User.Role targetRole = (request.getRole() != null) ? request.getRole() : user.getRole();
            if (targetRole != User.Role.staff) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can only assign a cinema to a STAFF user.");
            }
            Cinema cinema = cinemaRepository.findById(request.getCinemaId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cinema not found."));
            user.setCinema(cinema);
        } else {
            // Logic cũ: không làm gì nếu cinemaId null
        }
        User updatedUser = userRepository.save(user);
        return new UserResponseDTO(updatedUser);
    }

    @Override
    public void deleteUser(Integer id) {
        User user = getUserById(id);
        user.setIsActive(false);
        userRepository.save(user);
    }
}

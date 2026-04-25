package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Email
    @Size(max = 255)
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @Size(max = 255)
    @Column(name = "password_hash", nullable = false)
    @JsonIgnore // Không bao giờ trả về password hash trong API
    private String passwordHash;

    @NotBlank
    @Size(max = 255)
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Size(max = 20)
    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Size(max = 500)
    @Column(name = "avatar_url")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_id")
    private Cinema cinema;

    @Builder.Default
    private Integer points = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_tier", nullable = false)
    private MembershipTier membershipTier = MembershipTier.silver;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_tokens", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "token", columnDefinition = "TEXT")
    @Builder.Default
    @JsonIgnore // Không trả về danh sách token trong API
    private Set<String> tokens = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Booking> bookings = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Review> reviews = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Notification> notifications = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums for gender, role, and membership_tier
    public enum Gender {
        male, female, other
    }

    public enum Role {
        customer, admin, staff
    }

    public enum MembershipTier {
        silver, gold, platinum
    }
}

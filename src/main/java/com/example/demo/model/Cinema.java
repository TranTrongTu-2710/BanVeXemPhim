package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cinemas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cinema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String city;

    @Size(max = 100)
    private String district;

    @Size(max = 20)
    private String phone;

    @Email
    @Size(max = 255)
    private String email;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String facilities;

    @Builder.Default
    @OneToMany(mappedBy = "cinema", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Screen> screens = new ArrayList<>();

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

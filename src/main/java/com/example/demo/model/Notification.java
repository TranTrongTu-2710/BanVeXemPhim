package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // SỬA Ở ĐÂY: Cho phép user_id có thể là NULL cho thông báo hệ thống
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true) 
    @JsonBackReference("user-notification")
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String message;

    @Builder.Default
    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "related_id")
    private Integer relatedId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum NotificationType {
        booking, promotion, reminder, system
    }
}

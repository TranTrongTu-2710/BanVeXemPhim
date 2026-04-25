package com.example.demo.repository;

import com.example.demo.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    
    // Lấy thông báo cá nhân (cũ)
    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);
    
    // Lấy thông báo hệ thống (user = null)
    List<Notification> findByUserIsNullOrderByCreatedAtDesc();

    // Lấy tất cả thông báo liên quan đến user (cá nhân + hệ thống)
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId OR n.user IS NULL ORDER BY n.createdAt DESC")
    List<Notification> findAllForUser(@Param("userId") Integer userId);
}

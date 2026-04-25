package com.example.demo.controller;

import com.example.demo.model.Notification;
import com.example.demo.request.CreateBroadcastNotificationRequest;
import com.example.demo.request.UpdateNotificationRequest;
import com.example.demo.services.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications() {
        return ResponseEntity.ok(notificationService.getMyNotifications());
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Integer id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/broadcast")
    public ResponseEntity<Map<String, String>> sendBroadcastNotification(@Valid @RequestBody CreateBroadcastNotificationRequest request) {
        notificationService.sendBroadcastNotification(request);
        return ResponseEntity.ok(Map.of("message", "Broadcast notification sent successfully."));
    }

    @GetMapping("/system")
    public ResponseEntity<List<Notification>> getSystemNotifications() {
        return ResponseEntity.ok(notificationService.getSystemNotifications());
    }

    // Admin: Cập nhật thông báo
    @PutMapping("/{id}")
    public ResponseEntity<Notification> updateNotification(@PathVariable Integer id, @Valid @RequestBody UpdateNotificationRequest request) {
        return ResponseEntity.ok(notificationService.updateNotification(id, request));
    }

    // Admin: Xóa thông báo
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable Integer id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(Map.of("message", "Notification deleted successfully."));
    }
}

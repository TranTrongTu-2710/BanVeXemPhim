package com.example.demo.services;

import com.example.demo.model.Notification;
import com.example.demo.model.User;
import com.example.demo.request.CreateBroadcastNotificationRequest;
import com.example.demo.request.UpdateNotificationRequest;

import java.util.List;

public interface NotificationService {
    Notification createNotification(User user, Notification.NotificationType type, String title, String message, Integer relatedId);
    List<Notification> getMyNotifications();
    void markAsRead(Integer notificationId);
    void markAllAsRead();
    
    void sendBroadcastNotification(CreateBroadcastNotificationRequest request);
    List<Notification> getSystemNotifications();

    // Phương thức mới: Sửa thông báo
    Notification updateNotification(Integer id, UpdateNotificationRequest request);

    // Phương thức mới: Xóa thông báo
    void deleteNotification(Integer id);
}

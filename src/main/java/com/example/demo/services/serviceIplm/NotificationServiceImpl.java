package com.example.demo.services.serviceIplm;

import com.example.demo.model.Notification;
import com.example.demo.model.User;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.request.CreateBroadcastNotificationRequest;
import com.example.demo.request.UpdateNotificationRequest;
import com.example.demo.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public Notification createNotification(User user, Notification.NotificationType type, String title, String message, Integer relatedId) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .relatedId(relatedId)
                .build();
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getMyNotifications() {
        User currentUser = getCurrentUser();
        // Lấy cả thông báo cá nhân VÀ thông báo hệ thống
        return notificationRepository.findAllForUser(currentUser.getId());
    }

    @Override
    public void markAsRead(Integer notificationId) {
        User currentUser = getCurrentUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found."));

        // Nếu là thông báo hệ thống (user = null), không cho phép đánh dấu đã đọc (hoặc cần logic riêng như bảng user_notification_read)
        // Tạm thời chỉ cho phép đánh dấu đã đọc với thông báo cá nhân
        if (notification.getUser() == null) {
             // Có thể bỏ qua hoặc ném lỗi, ở đây ta bỏ qua để tránh lỗi client
             return; 
        }

        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to perform this action.");
        }
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead() {
        User currentUser = getCurrentUser();
        // Chỉ đánh dấu các thông báo cá nhân
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    @Override
    public void sendBroadcastNotification(CreateBroadcastNotificationRequest request) {
        // SỬA LỖI: Chỉ tạo MỘT bản ghi duy nhất với user = null
        Notification notification = Notification.builder()
                .user(null) // Quan trọng: user là null để đánh dấu là thông báo chung
                .type(Notification.NotificationType.system)
                .title(request.getTitle())
                .message(request.getMessage())
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getSystemNotifications() {
        // Lấy tất cả thông báo có user = null
        return notificationRepository.findByUserIsNullOrderByCreatedAtDesc();
    }

    @Override
    public Notification updateNotification(Integer id, UpdateNotificationRequest request) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));

        // Chỉ cho phép sửa thông báo hệ thống (hoặc thêm logic check quyền sở hữu nếu cần)
        // Ở đây giả định Admin có quyền sửa mọi thứ
        
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        
        return notificationRepository.save(notification);
    }

    @Override
    public void deleteNotification(Integer id) {
        if (!notificationRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found");
        }
        notificationRepository.deleteById(id);
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated.");
    }
}

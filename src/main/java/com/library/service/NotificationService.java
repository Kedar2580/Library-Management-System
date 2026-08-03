package com.library.service;

import com.library.model.Notification;
import com.library.model.NotificationType;
import com.library.model.User;
import com.library.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void notify(User user, NotificationType type, String title, String message) {
        Notification n = new Notification();
        n.setUser(user);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        notificationRepository.save(n);
    }

    public List<Notification> forUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public long unreadCount(User user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }

    public void markRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    public void markAllRead(User user) {
        notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user)
                .forEach(n -> {
                    n.setRead(true);
                    notificationRepository.save(n);
                });
    }
}

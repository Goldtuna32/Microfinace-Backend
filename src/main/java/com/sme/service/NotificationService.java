package com.sme.service;

import com.sme.entity.Notification;
import com.sme.entity.SmeLoanRegistration;
import com.sme.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    public void sendSystemNotification(Long accountId, String type, String message, Long smeLoanId) {
        try {
            Notification notification = new Notification();
            notification.setAccountId(accountId);
            notification.setType(type);
            notification.setMessage(message);

            // Optional: Link to the loan if provided
            if (smeLoanId != null) {
                SmeLoanRegistration loan = new SmeLoanRegistration();
                loan.setId(smeLoanId);
                notification.setSmeLoan(loan);
            }

            notificationRepository.save(notification);
            System.out.println("Notification saved for account: " + accountId +
                    ", type: " + type + ", loan: " + (smeLoanId != null ? smeLoanId : "none"));
        } catch (Exception e) {
            System.err.println("Failed to save notification for account " +
                    accountId + ": " + e.getMessage());
            throw new RuntimeException("Notification saving failed", e);
        }
    }

    // Retrieve unread notifications for an account
    public List<Notification> getUnreadNotifications(Long accountId) {
        return notificationRepository.findByAccountIdAndIsReadFalse(accountId);
    }

    // Mark a notification as read
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
    }
}

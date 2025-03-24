package com.sme.controller;

import com.sme.dto.NotificationDTO;
import com.sme.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<NotificationDTO> sendNotification(
            @RequestParam Long accountId,
            @RequestParam String type,
            @RequestParam String message,
            @RequestParam(required = false) Long smeLoanId) {
        NotificationDTO notificationDTO = notificationService.sendSystemNotification(accountId, type, message, smeLoanId);
        return ResponseEntity.ok(notificationDTO);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getAllUnreadNotifications() {
        List<NotificationDTO> notifications = notificationService.getAllUnreadNotifications();
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/mark-as-read/{notificationId}")
    public ResponseEntity<NotificationDTO> markAsRead(@PathVariable Long notificationId) {
        NotificationDTO updatedNotification = notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(updatedNotification);
    }
}
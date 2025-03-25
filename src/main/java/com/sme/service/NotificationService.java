package com.sme.service;

import com.sme.dto.NotificationDTO;
import com.sme.dto.SmeLoanRegistrationDTO; // Assuming this exists
import com.sme.entity.Notification;
import com.sme.entity.SmeLoanRegistration;
import com.sme.repository.NotificationRepository;
import com.sme.repository.SmeLoanRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SmeLoanRegistrationRepository smeLoanRegistrationRepository;

    @Autowired
    private SmeLoanRegistrationService smeLoanRegistrationService; // Inject the loan service

    // Existing sendSystemNotification method
    public NotificationDTO sendSystemNotification(Long accountId, String type, String message, Long smeLoanId) {
        Notification notification = new Notification();
        notification.setAccountId(accountId);
        notification.setType(type);
        notification.setMessage(message);

        if (smeLoanId != null) {
            Optional<SmeLoanRegistration> loanOpt = smeLoanRegistrationRepository.findById(smeLoanId);
            if (loanOpt.isPresent()) {
                notification.setSmeLoan(loanOpt.get());
            } else {
                throw new RuntimeException("SmeLoanRegistration with ID " + smeLoanId + " not found");
            }
        }

        Notification savedNotification = notificationRepository.save(notification);
        return mapToDTO(savedNotification);
    }

    // Fetch all unread notifications and pending loans
    public List<NotificationDTO> getAllUnreadNotifications() {
        List<NotificationDTO> unreadNotifications = notificationRepository.findByIsReadFalse()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        // Add pending loans as notifications
        List<NotificationDTO> pendingLoanNotifications = smeLoanRegistrationService.getPendingLoans()
                .stream()
                .map(this::mapLoanToNotificationDTO)
                .collect(Collectors.toList());

        unreadNotifications.addAll(pendingLoanNotifications);
        return unreadNotifications;
    }

    // Mark a notification as read
    public NotificationDTO markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        notification.setRead(true);
        Notification updatedNotification = notificationRepository.save(notification);
        return mapToDTO(updatedNotification);
    }

    // Map Notification entity to NotificationDTO
    private NotificationDTO mapToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setAccountId(notification.getAccountId());
        dto.setType(notification.getType());
        dto.setMessage(notification.getMessage());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setRead(notification.isRead());
        if (notification.getSmeLoan() != null) {
            dto.setSmeLoanId(notification.getSmeLoan().getId());
        }
        return dto;
    }

    // Map SmeLoanRegistrationDTO to NotificationDTO
    private NotificationDTO mapLoanToNotificationDTO(SmeLoanRegistrationDTO loan) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(loan.getId()); // Use loan ID as notification ID
        dto.setAccountId(loan.getCurrentAccountId()); // Assuming SmeLoanRegistrationDTO has accountId
        dto.setType("PENDING_LOAN");
        dto.setMessage("Pending Loan Application: " + loan.getId()); // Customize message
        dto.setRead(false); // Treat pending loans as unread
        dto.setSmeLoanId(loan.getId());
        return dto;
    }
}
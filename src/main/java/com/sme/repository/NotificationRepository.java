package com.sme.repository;

import com.sme.entity.Notification;
import com.sme.entity.SmeLoanRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByAccountIdAndIsReadFalse(Long accountId);

    boolean existsBySmeLoanAndCreatedAtBetween(SmeLoanRegistration smeLoan, LocalDateTime start, LocalDateTime end);

    // Optional: Find notifications by loan and unread status
    List<Notification> findBySmeLoanIdAndIsReadFalse(Long smeLoanId);
}
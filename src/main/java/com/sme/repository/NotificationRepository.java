package com.sme.repository;

import com.sme.entity.Notification;
import com.sme.entity.SmeLoanRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByAccountIdAndIsReadFalse(Long accountId); // Keep this for potential future use
    List<Notification> findByIsReadFalse(); // New method to fetch all unread notifications

    boolean existsBySmeLoanAndCreatedAtBetween(SmeLoanRegistration smeLoan, LocalDateTime start, LocalDateTime end);
    List<Notification> findBySmeLoanIdAndIsReadFalse(Long smeLoanId);
}
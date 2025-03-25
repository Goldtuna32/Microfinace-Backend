package com.sme.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "message", nullable = false, length = 255)
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @ManyToOne
    @JoinColumn(name = "sme_loan_id")
    private SmeLoanRegistration smeLoan;
}

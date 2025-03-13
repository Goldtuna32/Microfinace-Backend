package com.sme.dto;

import com.sme.entity.SmeLoanRegistration;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Long id;
    private Long accountId;
    private String type;
    private String message;
    private LocalDateTime createdAt;
    private boolean isRead;
    private Long smeLoanId;
}

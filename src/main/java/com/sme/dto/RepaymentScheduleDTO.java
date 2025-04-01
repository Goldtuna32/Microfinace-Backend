package com.sme.dto;

import com.sme.annotation.StatusConverter;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RepaymentScheduleDTO {
    private Long id;
    private LocalDate dueDate;
    private LocalDate graceEndDate;
    private BigDecimal interestAmount;
    private BigDecimal principalAmount;
    private BigDecimal principal_paid;
    private BigDecimal lateFee;
    private BigDecimal interestOverDue;
    @StatusConverter
    private Integer status;
    private BigDecimal remainingPrincipal;
    private BigDecimal remainingInterest; // Add this field if missing
    private LocalDateTime createdAt;
    private Boolean paidLate;
    private LocalDateTime lateFeePaidDate;

    // Add helper method to check if payment is complete
    public boolean isComplete() {
        return status != null && status == 2;
    }

    // Add helper method to check if payment is overdue
    public boolean isOverdue() {
        if (isComplete()) {
            return false;
        }

        LocalDate today = LocalDate.now();
        boolean datePassed = dueDate.isBefore(today);
        boolean hasUnpaidAmount = (remainingPrincipal != null && remainingPrincipal.compareTo(BigDecimal.ZERO) > 0)
                || (remainingInterest != null && remainingInterest.compareTo(BigDecimal.ZERO) > 0);
        boolean hasOverdueInterest = interestOverDue != null && interestOverDue.compareTo(BigDecimal.ZERO) > 0;

        return (datePassed && hasUnpaidAmount) || hasOverdueInterest;
    }
}
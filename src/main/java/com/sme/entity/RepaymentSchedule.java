package com.sme.entity;

import com.sme.annotation.StatusConverter;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "repayment_schedule")
public class RepaymentSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "grace_end", nullable = false)
    private LocalDate graceEndDate;

    @Column(name = "lastPaymentDate", nullable = true)
    private LocalDate lastPaymentDate;

    // Add helper method to get the correct start date for late fee calculation
    public LocalDate getLateFeeStartDate() {
        return lastPaymentDate != null ? lastPaymentDate : dueDate;
    }

    @Column(name = "interest_amount", nullable = false)
    private BigDecimal interestAmount = BigDecimal.ZERO;

    @Column(name = "principal_amount", nullable = false)
    private BigDecimal principalAmount = BigDecimal.ZERO;


    @Column(name = "late_fee", nullable = false)
    private BigDecimal lateFee = BigDecimal.ZERO;

    @Column(name = "interest_over_due", nullable = false)
    private BigDecimal interestOverDue = BigDecimal.ZERO;

    @StatusConverter
    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "remaining_principal", nullable = false)
    private BigDecimal remainingPrincipal = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_late")
    private Boolean paidLate = false;


    @ManyToOne
    @JoinColumn(name = "sme_loan_id", nullable = false)
    private SmeLoanRegistration smeLoan;
}

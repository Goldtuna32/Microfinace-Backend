package com.sme.entity;

import com.sme.annotation.StatusConverter;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Data
@Table(name = "repayment_transaction")
public class RepaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_date", nullable = false)
    private Timestamp paymentDate;

    @Column(name = "paid_principal", nullable = false)
    private BigDecimal paidPrincipal;

    @Column(name = "paid_interest", nullable = false)
    private BigDecimal paidInterest;

    @Column(name = "paid_late_fee", nullable = false)
    private BigDecimal paidLateFee;

    @Column(name = "late_fee_paid_date")
    private LocalDateTime lateFeePaidDate;

    @Column(name = "paid_IOD", nullable = false)
    private BigDecimal paidIOD;

    @Column(name = "remaining_principal", nullable = false)
    private BigDecimal remainingPrincipal;

    @StatusConverter
    @Column(name = "status", nullable = false)
    private Integer status;

    @ManyToOne
    @JoinColumn(name = "current_account_id", nullable = false)
    private CurrentAccount currentAccount;

    @ManyToOne
    @JoinColumn(name = "repayment_schedule_id", nullable = false)
    private RepaymentSchedule repaymentSchedule;
}

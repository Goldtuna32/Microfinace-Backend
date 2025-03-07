package com.sme.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sme.annotation.StatusConverter;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "sme_loan_registration")
public class SmeLoanRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "interest_rate", nullable = false, precision = 15, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "late_fee_rate", nullable = false, precision = 15, scale = 2)
    private BigDecimal late_fee_rate;

    @Column(name = "ninety_day_late_fee_rate", nullable = false, precision = 15, scale = 2)
    private BigDecimal ninety_day_late_fee_rate;

    @Column(name = "one_hundred_and_eighty_day_late_fee_rate", nullable = false, precision = 15, scale = 2)
    private BigDecimal one_hundred_and_eighty_late_fee_rate;

    @Column(name = "grace_period", nullable = false)
    private Integer gracePeriod;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "repayment_duration", nullable = false)
    private Long repaymentDuration;

    @Column(name = "document_fee", nullable = false)
    private BigDecimal documentFee;

    @Column(name = "service_charges", nullable = false)
    private BigDecimal serviceCharges;

    @StatusConverter
    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "repayment_start_date")
    private LocalDateTime repaymentStartDate;

    @Column(name = "current_account_id", insertable = false, updatable = false) // Managed by the relationship
    private Long currentAccountId;

    @ManyToOne
    @JoinColumn(name = "current_account_id", nullable = false)
    private CurrentAccount currentAccount;

    @OneToMany(mappedBy = "smeLoan", cascade = CascadeType.ALL)
    private List<RepaymentSchedule> repaymentSchedules;

    @OneToMany(mappedBy = "smeLoan", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<SmeLoanCollateral> collaterals; // Always initialized
}

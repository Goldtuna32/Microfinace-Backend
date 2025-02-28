package com.sme.entity;

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

    @Column(name = "grace_period", nullable = false)
    private Integer gracePeriod;


    @Column(name = "repayment_duration", nullable = false)
    private Long repaymentDuration;

    @Column(name = "document_fee", nullable = false)
    private BigDecimal documentFee;

    @Column(name = "service_charges", nullable = false)
    private BigDecimal serviceCharges;

    @StatusConverter
    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "repayment_start_date")
    private LocalDateTime repaymentStartDate;

    @ManyToOne
    @JoinColumn(name = "current_account_id", nullable = false)
    private CurrentAccount currentAccount;

    @OneToMany(mappedBy = "smeLoan", cascade = CascadeType.ALL)
    private List<RepaymentSchedule> repaymentSchedules;

    @OneToMany(mappedBy = "smeLoan", cascade = CascadeType.ALL)
    private List<SmeLoanCollateral> smeLoanCollaterals; // Always initialized
}




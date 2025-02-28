package com.sme.entity;

import com.sme.annotation.StatusConverter;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "hp_registration")
public class HpRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hp_number", nullable = false, unique = true)
    private String hpNumber;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "loan_amount")
    private BigDecimal loanAmount;

    @Column(name = "down_payment")
    private BigDecimal downPayment;

    @Column(name = "loan_term")
    private Integer loanTerm;

    @Column(name = "interest_rate")
    private String interestRate;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @StatusConverter
    @Column(name = "status")
    private Integer status;


    @Column(name = "current_account_id", nullable = false)
    private Long currentAccountId;

    @Column(name = "hp_product_id", nullable = false)
    private Long hpProductId;
}

package com.sme.entity;

import com.sme.annotation.StatusConverter;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Column(name = "grace_period", nullable = false)
    private Integer gracePeriod;

    @Column(name = "loan_amount")
    private BigDecimal loanAmount;

    @Column(name = "down_payment")
    private BigDecimal downPayment;

    @Column(name = "loan_term")
    private Integer loanTerm;

    @Column(name = "interest_rate")
    private String interestRate;

    @Column(name = "late_fee_rate", nullable = false, precision = 15, scale = 2)
    private BigDecimal late_fee_rate;

    @Column(name = "ninety_day_late_fee_rate", nullable = false, precision = 15, scale = 2)
    private BigDecimal ninety_day_late_fee_rate;

    @Column(name = "one_hundred_and_eighty_day_late_fee_rate", nullable = false, precision = 15, scale = 2)
    private BigDecimal one_hundred_and_eighty_late_fee_rate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @StatusConverter
    @Column(name = "status")
    private Integer status;


    @OneToOne
    @JoinColumn(name = "current_account_id")
    private CurrentAccount currentAccount;


    @Column(name = "hp_product_id", nullable = false)
    private Long hpProductId;


}

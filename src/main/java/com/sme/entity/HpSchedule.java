package com.sme.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@Table(name = "hp_schedule", schema = "mydb")
public class HpSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date")
    private Timestamp date;

    @Column(name = "interest_amount")
    private Long interestAmount;

    @Column(name = "principal_amount")
    private Long principalAmount;

    @Column(name = "late_day")
    private Long lateDay;

    @Column(name = "late_fee")
    private BigDecimal lateFee;

    @Column(name = "principal_od")
    private BigDecimal principalOd;

    @Column(name = "interest_od")
    private BigDecimal interestOd;

    @Column(name = "installment_no")
    private String installmentNo;

    @Column(name = "hp_registration_id", nullable = false)
    private Long hpRegistrationId;

    @Column(name = "late_fee_paid_date")
    private LocalDateTime lateFeePaidDate;

 }

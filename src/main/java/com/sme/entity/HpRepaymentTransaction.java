package com.sme.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Data
@Table(name = "hp_repayment_transaction")
public class HpRepaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_date")
    private Timestamp paymentDate;

    @Column(name = "paid_principal")
    private String paidPrincipal;

    @Column(name = "paid_interest")
    private String paidInterest;

    @Column(name = "paid_late_fee")
    private String paidLateFee;

    @Column(name = "paid_interest_IOD")
    private String paidInterestIOD;

    @Column(name = "paid_principal_IOD")
    private String paidPrincipalIOD;

    @Column(name = "paid_interest_late_fee")
    private String paidInterestLateFee;

    @Column(name = "paid_principal_late_fee")
    private String paidPrincipalLateFee;

    @Column(name = "hp_schedule_id", nullable = false)
    private Long hpScheduleId;

    @Column(name = "current_account_id", nullable = false)
    private Long currentAccountId;

 }

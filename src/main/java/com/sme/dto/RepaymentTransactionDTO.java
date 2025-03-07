package com.sme.dto;

import com.sme.annotation.StatusConverter;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RepaymentTransactionDTO {
    private Long id;
    private LocalDateTime paymentDate;
    private BigDecimal paidPrincipal;
    private BigDecimal paidInterest;
    private BigDecimal paidLateFee;
    private LocalDateTime lateFeePaidDate;
    private BigDecimal paidIOD;
    private BigDecimal remainingPrincipal;
    @StatusConverter
    private Integer status;
    private Long currentAccountId;
    private Long repaymentScheduleId;
}

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
    private BigDecimal lateFee;
    private BigDecimal interestOverDue;
    @StatusConverter
    private Integer status;
    private BigDecimal remainingPrincipal;
    private LocalDateTime createdAt;
    private Boolean paidLate;
    private LocalDateTime lateFeePaidDate;
}

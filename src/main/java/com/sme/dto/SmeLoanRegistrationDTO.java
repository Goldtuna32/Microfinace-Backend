package com.sme.dto;

import com.sme.annotation.StatusConverter;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SmeLoanRegistrationDTO {
    private Long id;
    private BigDecimal loanAmount;
    private BigDecimal interestRate;
    private Integer gracePeriod;
    private Long repaymentDuration;
    private BigDecimal documentFee;
    private BigDecimal serviceCharges;
    @StatusConverter
    private Integer status;
    private LocalDateTime dueDate;
    private LocalDateTime repaymentStartDate;
    private Long currentAccountId;
    private List<Long> collateralIds;
}

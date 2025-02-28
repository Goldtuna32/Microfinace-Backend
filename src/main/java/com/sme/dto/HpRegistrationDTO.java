package com.sme.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HpRegistrationDTO {
    private Long id;
    private String hpNumber;
    private LocalDateTime createdDate;
    private BigDecimal loanAmount;
    private BigDecimal downPayment;
    private Integer loanTerm;
    private String interestRate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer status;
    private Long disbursementId;
    private Long currentAccountId;
    private LocalDateTime disbursementDate;
    private Long hpProductId;
}

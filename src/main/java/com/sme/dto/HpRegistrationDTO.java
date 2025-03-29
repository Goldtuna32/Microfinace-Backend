package com.sme.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
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
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
    private Integer gracePeriod;
    private Long disbursementId;
    private BigDecimal late_fee_rate;
    private BigDecimal ninety_day_late_fee_rate;
    private BigDecimal one_hundred_and_eighty_late_fee_rate;
    private Long currentAccountId;
    private LocalDateTime disbursementDate;
    private Long hpProductId;
}

package com.sme.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private LocalDateTime startDate;
    @JsonIgnore
    private LocalDateTime endDate;
    private Integer status;
    private Long currentAccountId;
    private Long hpProductId;
}

package com.sme.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RecentLoanDTO {
    private String serialCode;
    private String customerName;
    private BigDecimal loanAmount;
    private int status;
    private LocalDateTime applicationDate;
}

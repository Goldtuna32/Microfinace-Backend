package com.sme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmeLoanCollateralDTO {
    private Long id;
    private Long loanId;
    private Long collateralId;
    private BigDecimal collateralAmount;
    private String description;

}

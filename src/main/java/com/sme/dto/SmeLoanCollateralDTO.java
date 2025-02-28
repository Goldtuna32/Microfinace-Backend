package com.sme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmeLoanCollateralDTO {
    private Long id;
    private Long loanId;
    private Long collateralId;
}

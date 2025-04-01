package com.sme.dto;

import com.sme.annotation.StatusConverter;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmeLoanCollateralDTO {
    private Long id;
    private Long loanId;
    private Long collateralId;
    private BigDecimal collateralAmount;
    private String description;
    private String collateralCode;
    private BigDecimal value;
    @StatusConverter
    private Integer status;
    private LocalDateTime date; // Add this if relevant

}

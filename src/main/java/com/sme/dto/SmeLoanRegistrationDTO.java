package com.sme.dto;

import com.sme.annotation.StatusConverter;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SmeLoanRegistrationDTO {
    private Long id;
    private String serialCode;
    private BigDecimal loanAmount;
    private BigDecimal interestRate;
    private BigDecimal late_fee_rate;
    private BigDecimal ninety_day_late_fee_rate;
    private BigDecimal one_hundred_and_eighty_day_late_fee_rate;
    private Integer gracePeriod;
    private Long repaymentDuration;
    private BigDecimal documentFee;
    private BigDecimal serviceCharges;
    @StatusConverter
    private Integer status;
    private LocalDateTime dueDate;
    private LocalDateTime repaymentStartDate;
    private Long currentAccountId;
    private BigDecimal totalCollateralAmount;
    private String accountNumber;
    private CurrentAccountDTO currentAccountDetails;
    private CIFDTO cifDetails;
    private CIFDTO cif; // Add this for frontend compatibility
    private List<SmeLoanCollateralDTO> collaterals;

    // Add this setter for frontend compatibility
    public void setCif(CIFDTO cif) {
        this.cif = cif;
        this.cifDetails = cif; // Also set cifDetails for consistency
    }
}
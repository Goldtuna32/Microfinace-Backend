package com.sme.dto;

import com.sme.annotation.StatusConverter;
import lombok.Data;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SmeLoanRegistrationDTO {
    private Long id;
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
    private CIFDTO cif;
    private String accountNumber;
    private List<SmeLoanCollateralDTO> collaterals;

    @Data
    public static class CIFDTO {
        private Long id;
        private String name;
        private String serialNumber;
        private String nrcNumber;
        private String email;
    }
}

package com.sme.dto;

import com.sme.annotation.StatusConverter;
import com.sme.entity.Status;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class CurrentAccountDTO {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    @StatusConverter
    private Integer status;

    private Date dateCreated;
    private BigDecimal holdAmount;
    private Long cifId;

    private BigDecimal maximumBalance;
    private BigDecimal minimumBalance;
}


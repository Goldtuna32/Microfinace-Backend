package com.sme.dto;

import com.sme.annotation.StatusConverter;
import com.sme.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

    public CurrentAccountDTO(Long id, String accountNumber, Long cifId) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.cifId = cifId;
    }

    public CurrentAccountDTO(Long id, String accountNumber, BigDecimal balance, BigDecimal maximumBalance, BigDecimal minimumBalance, Integer status) {
    }
}


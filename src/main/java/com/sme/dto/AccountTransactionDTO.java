package com.sme.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class AccountTransactionDTO {
    private Long id;
    private String transactionType; // CREDIT or DEBIT
    private BigDecimal amount;
    private String transactionDescription;
    private Integer status;
    private Date transactionDate;
    private Long currentAccountId;
}

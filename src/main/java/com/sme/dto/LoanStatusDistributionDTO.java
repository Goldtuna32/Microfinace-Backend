package com.sme.dto;

import lombok.Data;

@Data
public class LoanStatusDistributionDTO {
    private long approved;
    private long pending;
    private long rejected;
}
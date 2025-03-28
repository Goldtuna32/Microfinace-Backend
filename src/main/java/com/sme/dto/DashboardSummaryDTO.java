package com.sme.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardSummaryDTO {
    private long totalCifs;
    private long activeCifs;
    private long totalSmeLoans;
    private long pendingSmeLoans;
    private long totalHpRegistrations;
    private long activeHpRegistrations;
    private BigDecimal totalCollateralValue;
    private BigDecimal avgCollateralPerLoan;
}
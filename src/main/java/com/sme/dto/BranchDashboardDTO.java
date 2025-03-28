package com.sme.dto;

import lombok.Data;

@Data
public class BranchDashboardDTO {
    private Long branchId;
    private String branchName;
    private int cifCount;
    private int activeCifCount;
    private int currentAccountCount;
    private int collateralCount;
    private int smeLoanCount;
    private int pendingSmeLoanCount;
    private int activeSmeLoanCount;
    private int transactionCount;
    private int hpProductCount;
    private int hpRegistrationCount;
    private int activeHpRegistrationCount;
    private int dealerRegistrationCount;
}
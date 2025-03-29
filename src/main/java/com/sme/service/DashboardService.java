package com.sme.service;

import com.sme.dto.*;

import java.util.List;

public interface DashboardService {
    DashboardSummaryDTO getSummary();
    LoanStatusDistributionDTO getLoanStatusDistribution();
    MonthlyApplicationsDTO getMonthlyApplications();
    List<RecentLoanDTO> getRecentLoans();
    HpProductDistributionDTO getHpProductDistribution();
}
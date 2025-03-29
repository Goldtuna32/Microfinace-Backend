package com.sme.controller;

import com.sme.dto.*;
import com.sme.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getDashboardSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/loan-status-distribution")
    public ResponseEntity<LoanStatusDistributionDTO> getLoanStatusDistribution() {
        return ResponseEntity.ok(dashboardService.getLoanStatusDistribution());
    }

    @GetMapping("/monthly-applications")
    public ResponseEntity<MonthlyApplicationsDTO> getMonthlyApplications() {
        return ResponseEntity.ok(dashboardService.getMonthlyApplications());
    }

    @GetMapping("/recent-loans")
    public ResponseEntity<List<RecentLoanDTO>> getRecentLoans() {
        return ResponseEntity.ok(dashboardService.getRecentLoans());
    }

    @GetMapping("/hp-product-distribution")
    public ResponseEntity<HpProductDistributionDTO> getHpProductDistribution() {
        return ResponseEntity.ok(dashboardService.getHpProductDistribution());
    }
}
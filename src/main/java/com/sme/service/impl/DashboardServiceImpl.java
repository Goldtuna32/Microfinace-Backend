package com.sme.service.impl;

import com.sme.dto.*;
import com.sme.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final CIFService cifService;
    private final SmeLoanRegistrationService smeLoanService;
    private final HpRegistrationService hpRegistrationService;
    private final CollateralService collateralService;
    private final HpProductService hpProductService;

    @Override
    public DashboardSummaryDTO getSummary() {
        DashboardSummaryDTO summary = new DashboardSummaryDTO();
        summary.setTotalCifs(cifService.getTotalCifCount());
        summary.setActiveCifs(cifService.getActiveCifCount());
        summary.setTotalSmeLoans(smeLoanService.getTotalLoanCount());
        summary.setPendingSmeLoans(smeLoanService.getPendingLoanCount());
        summary.setTotalHpRegistrations(hpRegistrationService.getTotalHpCount());
        summary.setActiveHpRegistrations(hpRegistrationService.getActiveHpCount());
        summary.setTotalCollateralValue(collateralService.getTotalCollateralValue());
        summary.setAvgCollateralPerLoan(collateralService.getAverageCollateralPerLoan());
        return summary;
    }

    @Override
    public LoanStatusDistributionDTO getLoanStatusDistribution() {
        LoanStatusDistributionDTO distribution = new LoanStatusDistributionDTO();
        distribution.setApproved(smeLoanService.countByStatus(1)); // Assuming 1 is approved status
        distribution.setPending(smeLoanService.countByStatus(0)); // Assuming 0 is pending status
        distribution.setRejected(smeLoanService.countByStatus(2)); // Assuming 2 is rejected status
        return distribution;
    }

    @Override
    public MonthlyApplicationsDTO getMonthlyApplications() {
        MonthlyApplicationsDTO monthlyData = new MonthlyApplicationsDTO();

        // Initialize with 12 months (0 values)
        List<Long> smeLoans = new ArrayList<>(Collections.nCopies(12, 0L));
        List<Long> hpRegistrations = new ArrayList<>(Collections.nCopies(12, 0L));

        // Fill actual data for SME loans
        List<Object[]> smeData = smeLoanService.countLoansByMonth();
        for (Object[] data : smeData) {
            int month = (int) data[0]; // month index (0-11)
            long count = (long) data[1];
            smeLoans.set(month, count);
        }

        // Fill actual data for HP registrations
        List<Object[]> hpData = hpRegistrationService.countHpByMonth();
        for (Object[] data : hpData) {
            int month = (int) data[0]; // month index (0-11)
            long count = (long) data[1];
            hpRegistrations.set(month, count);
        }

        monthlyData.setSmeLoans(smeLoans);
        monthlyData.setHpRegistrations(hpRegistrations);
        return monthlyData;
    }

    @Override
    public List<RecentLoanDTO> getRecentLoans() {
        return smeLoanService.findRecentLoans(5).stream()
                .map(loan -> {
                    RecentLoanDTO dto = new RecentLoanDTO();
                    dto.setSerialCode(loan.getSerialCode());
                    dto.setCustomerName(loan.getCurrentAccount().getCif().getName());
                    dto.setLoanAmount(loan.getLoanAmount());
                    dto.setStatus(loan.getStatus());
                    dto.setApplicationDate(loan.getRepaymentStartDate());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public HpProductDistributionDTO getHpProductDistribution() {
        HpProductDistributionDTO distribution = new HpProductDistributionDTO();
        List<String> productTypes = new ArrayList<>();
        List<Long> counts = new ArrayList<>();

        List<Object[]> productData = hpProductService.countProductsByType();
        for (Object[] data : productData) {
            productTypes.add((String) data[0]); // Product type name
            counts.add((Long) data[1]); // Count
        }

        distribution.setProductTypes(productTypes);
        distribution.setCounts(counts);
        return distribution;
    }
}
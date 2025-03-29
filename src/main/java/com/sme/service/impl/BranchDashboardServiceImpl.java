package com.sme.service.impl;

import com.sme.dto.BranchDashboardDTO;
import com.sme.dto.BranchStatsDTO;
import com.sme.entity.Branch;
import com.sme.exception.BranchNotFoundException;
import com.sme.repository.*;
import com.sme.service.BranchDashboardService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchDashboardServiceImpl implements BranchDashboardService {

    private final CIFRepository cifRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final CollateralRepository collateralRepository;
    private final SmeLoanRegistrationRepository smeLoanRepository;
    private final AccountTransactionRepository transactionRepository;
    private final HpProductRepository hpProductRepository;
    private final HpRegistrationRepository hpRegistrationRepository;
    private final DealerRegistrationRepository dealerRegistrationRepository;
    private final BranchRepository branchRepository;

    @Override
    public BranchDashboardDTO getBranchDashboardStats(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new BranchNotFoundException("Branch not found"));

        BranchDashboardDTO dto = new BranchDashboardDTO();
        dto.setBranchId(branchId);
        dto.setBranchName(branch.getName());

        // Customer Information
        dto.setCifCount(cifRepository.countByBranchId(branchId));
        dto.setActiveCifCount(cifRepository.countByBranchIdAndStatus(branchId, 1));

        // Account Information
        dto.setCurrentAccountCount(currentAccountRepository.countByBranchId(branchId));
        dto.setTransactionCount(transactionRepository.countByBranchId(branchId));

        // Collateral Information
        dto.setCollateralCount(collateralRepository.countByBranchId(branchId));

        // SME Loan Information
        dto.setSmeLoanCount(smeLoanRepository.countByBranchId(branchId));
        dto.setPendingSmeLoanCount(smeLoanRepository.countByBranchIdAndStatus(branchId, 0));
        dto.setActiveSmeLoanCount(smeLoanRepository.countByBranchIdAndStatus(branchId, 1));

        // HP (Hire Purchase) Information
        dto.setHpProductCount(hpProductRepository.countByBranchId(branchId));
        dto.setHpRegistrationCount(hpRegistrationRepository.countByBranchId(branchId));
        dto.setActiveHpRegistrationCount(hpRegistrationRepository.countByBranchIdAndStatus(branchId, 1));

        // Dealer Information
        dto.setDealerRegistrationCount(dealerRegistrationRepository.countByBranchId(branchId));

        return dto;
    }

    @Override
    @Transactional
    public List<BranchStatsDTO> getAllBranchesStats() {
        List<Branch> branches = branchRepository.findAll();
        return branches.stream().map(branch -> {
            Long branchId = branch.getId();

            BranchStatsDTO dto = new BranchStatsDTO();
            dto.setBranchId(branchId);
            dto.setBranchName(branch.getName());

            Map<String, Integer> stats = new HashMap<>();
            stats.put("cifCount", cifRepository.countByBranchId(branchId));
            stats.put("activeCifCount", cifRepository.countByBranchIdAndStatus(branchId, 1));
            stats.put("currentAccountCount", currentAccountRepository.countByBranchId(branchId));
            stats.put("transactionCount", transactionRepository.countByBranchId(branchId));
            stats.put("collateralCount", collateralRepository.countByBranchId(branchId));
            stats.put("smeLoanCount", smeLoanRepository.countByBranchId(branchId));
            stats.put("hpProductCount", hpProductRepository.countByBranchId(branchId));
            stats.put("hpRegistrationCount", hpRegistrationRepository.countByBranchId(branchId));
            stats.put("dealerRegistrationCount", dealerRegistrationRepository.countByBranchId(branchId));

            dto.setStats(stats);
            return dto;
        }).collect(Collectors.toList());
    }
}
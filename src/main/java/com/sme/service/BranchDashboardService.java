package com.sme.service;

import com.sme.dto.BranchDashboardDTO;
import com.sme.dto.BranchStatsDTO;

import java.util.List;

public interface BranchDashboardService {
    BranchDashboardDTO getBranchDashboardStats(Long branchId);

    List<BranchStatsDTO> getAllBranchesStats();
}

package com.sme.dto;

import lombok.Data;

import java.util.Map;

@Data
public class BranchStatsDTO {
    private Long branchId;
    private String branchName;
    private Map<String, Integer> stats;
}
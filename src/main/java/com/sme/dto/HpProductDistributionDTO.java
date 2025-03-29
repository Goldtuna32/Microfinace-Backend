package com.sme.dto;

import lombok.Data;

import java.util.List;

@Data
public class HpProductDistributionDTO {
    private List<String> productTypes;
    private List<Long> counts;
}
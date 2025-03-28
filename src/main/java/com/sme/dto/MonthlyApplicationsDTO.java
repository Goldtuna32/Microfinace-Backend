package com.sme.dto;

import lombok.Data;

import java.util.List;

@Data
public class MonthlyApplicationsDTO {
    private List<Long> smeLoans;
    private List<Long> hpRegistrations;
}
package com.sme.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class BranchDetailDTO {
    private Long id;
    private String branchName;
    private String branchCode;
    private String phoneNumber;
    private String email;
    private Date createdDate;
    private Date updatedDate;
    private Integer status;
    private AddressDTO address;

    // Counts
    private Long totalCifs;
    private Long totalCurrentAccounts;
    private Long totalTransactions;
    private Long totalCollaterals;
    private Long totalDealers;
    private Long ongoingSmeLoans;
    private Long ongoingHpLoans;

    // Sample Records
    private List<CIFDTO> recentCifs;
    private List<CurrentAccountDTO> recentCurrentAccounts;
    private List<AccountTransactionDTO> recentTransactions;
    private List<CollateralDTO> recentCollaterals;
    private List<DealerRegistrationDTO> recentDealers;
    private List<SmeLoanRegistrationDTO> recentSmeLoans;
    private List<HpRegistrationDTO> recentHpLoans;
}
package com.sme.service.impl;

import com.sme.dto.SmeLoanCollateralDTO;
import com.sme.entity.Collateral;
import com.sme.entity.SmeLoanCollateral;
import com.sme.entity.SmeLoanRegistration;
import com.sme.repository.CollateralRepository;
import com.sme.repository.SmeLoanCollateralRepository;
import com.sme.repository.SmeLoanRegistrationRepository;
import com.sme.service.SmeLoanCollateralService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SmeLoanCollateralServiceImpl implements SmeLoanCollateralService {

    private final SmeLoanCollateralRepository loanCollateralRepository;
    private final SmeLoanRegistrationRepository loanRepository;
    private final CollateralRepository collateralRepository;

    @Transactional
    public SmeLoanCollateralDTO linkCollateralToLoan(SmeLoanCollateralDTO dto) {
        SmeLoanRegistration loan = loanRepository.findById(dto.getLoanId())
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + dto.getLoanId()));

        Collateral collateral = collateralRepository.findById(dto.getCollateralId())
                .orElseThrow(() -> new RuntimeException("Collateral not found with ID: " + dto.getCollateralId()));

        SmeLoanCollateral loanCollateral = new SmeLoanCollateral();
        loanCollateral.setSmeLoan(loan);
        loanCollateral.setCollateral(collateral);

        loanCollateral = loanCollateralRepository.save(loanCollateral);
        dto.setId(loanCollateral.getId());
        return dto;
    }

    @Override
    public List<SmeLoanCollateralDTO> getCollateralsForLoan(Long loanId) {
        return loanCollateralRepository.findBySmeLoanId(loanId)
                .stream()
                .map(loanCollateral -> new SmeLoanCollateralDTO(
                        loanCollateral.getId(),
                        loanCollateral.getSmeLoan().getId(),
                        loanCollateral.getCollateral().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SmeLoanCollateralDTO> getLoansForCollateral(Long collateralId) {
        return loanCollateralRepository.findByCollateralId(collateralId)
                .stream()
                .map(loanCollateral -> new SmeLoanCollateralDTO(
                        loanCollateral.getId(),
                        loanCollateral.getSmeLoan().getId(),
                        loanCollateral.getCollateral().getId()))
                .collect(Collectors.toList());
    }
}

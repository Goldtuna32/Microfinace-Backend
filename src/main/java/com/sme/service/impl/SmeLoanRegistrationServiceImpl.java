package com.sme.service.impl;

import com.sme.dto.SmeLoanRegistrationDTO;
import com.sme.entity.Collateral;
import com.sme.entity.SmeLoanCollateral;
import com.sme.entity.SmeLoanRegistration;
import com.sme.repository.CollateralRepository;
import com.sme.repository.SmeLoanCollateralRepository;
import com.sme.repository.SmeLoanRegistrationRepository;
import com.sme.service.SmeLoanRegistrationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SmeLoanRegistrationServiceImpl implements SmeLoanRegistrationService {


    private final SmeLoanRegistrationRepository smeLoanRegistrationRepository;
    private final SmeLoanCollateralRepository smeLoanCollateralRepository;
    private final CollateralRepository collateralRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<SmeLoanRegistrationDTO> getAllLoans() {
        return smeLoanRegistrationRepository.findAll().stream()
                .map(loan -> modelMapper.map(loan, SmeLoanRegistrationDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public SmeLoanRegistrationDTO getLoanById(Long id) {
        return smeLoanRegistrationRepository.findById(id)
                .map(loan -> modelMapper.map(loan, SmeLoanRegistrationDTO.class))
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + id));
    }

    @Transactional
    @Override
    public SmeLoanRegistrationDTO createLoan(SmeLoanRegistrationDTO dto) {
        SmeLoanRegistration loan = modelMapper.map(dto, SmeLoanRegistration.class);
        loan = smeLoanRegistrationRepository.save(loan);
        return modelMapper.map(loan, SmeLoanRegistrationDTO.class);
    }

    @Override
    @Transactional
    public SmeLoanRegistration registerLoan(SmeLoanRegistration loan, List<SmeLoanCollateral> loanCollaterals) {
        BigDecimal totalCollateralAmount = loanCollaterals.stream()
                .map(smeLoanCollateral -> smeLoanCollateral.getCollateralAmount() == null ? BigDecimal.ZERO : smeLoanCollateral.getCollateralAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.println("Requested Loan Amount: " + loan.getLoanAmount());
        System.out.println("Total Collateral Amount: " + totalCollateralAmount);

        if (loan.getLoanAmount().compareTo(totalCollateralAmount) > 0) {
            throw new IllegalArgumentException("Loan amount cannot exceed total collateral amount.");
        }


        // Save Loan
        SmeLoanRegistration savedLoan = smeLoanRegistrationRepository.save(loan);

        for (SmeLoanCollateral smeLoanCollateral : loanCollaterals) {
            // Validate and set Collateral
            Collateral collateral = collateralRepository.findById(smeLoanCollateral.getCollateral().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Collateral not found with ID: " + smeLoanCollateral.getCollateral().getId()));

            smeLoanCollateral.setCollateral(collateral);
            smeLoanCollateral.setSmeLoan(savedLoan);

            // Ensure collateralAmount is not null before saving
            if (smeLoanCollateral.getCollateralAmount() == null) {
                throw new IllegalArgumentException("Collateral amount cannot be null for collateral ID: " + collateral.getId());
            }

            // Save SmeLoanCollateral
            smeLoanCollateralRepository.save(smeLoanCollateral);
        }

        return savedLoan;
    }

}

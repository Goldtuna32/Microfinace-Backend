package com.sme.service.impl;

import com.sme.dto.LoanRegistrationRequest;
import com.sme.dto.SmeLoanCollateralDTO;
import com.sme.dto.SmeLoanRegistrationDTO;
import com.sme.entity.*;
import com.sme.repository.*;
import com.sme.service.RepaymentScheduleService;
import com.sme.service.SmeLoanRegistrationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SmeLoanRegistrationServiceImpl implements SmeLoanRegistrationService {


    private final SmeLoanRegistrationRepository smeLoanRegistrationRepository;
    private final SmeLoanCollateralRepository smeLoanCollateralRepository;
    private final CollateralRepository collateralRepository;

    @Autowired
    private RepaymentScheduleService repaymentScheduleService;

    @Autowired
    private CIFRepository cifRepository;

    @Autowired
    private CurrentAccountRepository currentAccountRepository;


    @Override
    public SmeLoanRegistrationDTO getLoanById(Long id) {
        return mapToDTO(id);
    }

    @Override
    @Transactional
    public SmeLoanRegistrationDTO registerLoan(LoanRegistrationRequest request) {
        System.out.println("Received Request: " + request);
        SmeLoanRegistration loan = new SmeLoanRegistration();
        SmeLoanRegistrationDTO loanDTO = request.getLoan();
        loan.setLoanAmount(loanDTO.getLoanAmount());
        loan.setInterestRate(loanDTO.getInterestRate());
        loan.setGracePeriod(loanDTO.getGracePeriod());
        loan.setRepaymentDuration(loanDTO.getRepaymentDuration());
        loan.setDocumentFee(loanDTO.getDocumentFee());
        loan.setServiceCharges(loanDTO.getServiceCharges());
        loan.setStatus(loanDTO.getStatus());
        loan.setDueDate(loanDTO.getDueDate());
        loan.setRepaymentStartDate(loanDTO.getRepaymentStartDate());

        Long currentAccountId = loanDTO.getCurrentAccountId();
        if (currentAccountId == null) {
            throw new IllegalArgumentException("CurrentAccount ID is required.");
        }
        CurrentAccount currentAccount = currentAccountRepository.findById(currentAccountId)
                .orElseThrow(() -> new IllegalArgumentException("CurrentAccount not found with ID: " + currentAccountId));
        loan.setCurrentAccount(currentAccount);

        // Create collaterals without setting smeLoan yet
        List<SmeLoanCollateral> loanCollaterals = request.getCollaterals().stream()
                .map(dto -> {
                    if (dto.getCollateralId() == null) {
                        throw new IllegalArgumentException("Collateral ID is required.");
                    }
                    SmeLoanCollateral coll = new SmeLoanCollateral();
                    coll.setCollateralAmount(dto.getCollateralAmount());
                    Collateral collateral = collateralRepository.findById(dto.getCollateralId())
                            .orElseThrow(() -> new IllegalArgumentException("Collateral not found with ID: " + dto.getCollateralId()));
                    coll.setCollateral(collateral);
                    return coll; // Don’t set smeLoan here
                })
                .collect(Collectors.toList());

        BigDecimal totalCollateralAmount = loanCollaterals.stream()
                .map(coll -> coll.getCollateralAmount() == null ? BigDecimal.ZERO : coll.getCollateralAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (loan.getLoanAmount().compareTo(totalCollateralAmount) > 0) {
            throw new IllegalArgumentException("Loan amount cannot exceed total collateral amount.");
        }

        // Save the loan first to get its ID
        SmeLoanRegistration savedLoan = smeLoanRegistrationRepository.save(loan);

        // Now set the smeLoan reference and save collaterals
        for (SmeLoanCollateral coll : loanCollaterals) {
            coll.setSmeLoan(savedLoan);
            smeLoanCollateralRepository.save(coll);
        }

        return mapToDTO(savedLoan.getId());
    }

    @Override
    public SmeLoanRegistrationDTO updateLoan(Long id, SmeLoanRegistrationDTO dto) {
        SmeLoanRegistration loan = smeLoanRegistrationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found with ID: " + id));

        // Update editable fields
        loan.setLoanAmount(dto.getLoanAmount());
        loan.setInterestRate(dto.getInterestRate());
        loan.setGracePeriod(dto.getGracePeriod());
        loan.setRepaymentDuration(dto.getRepaymentDuration());
        loan.setDocumentFee(dto.getDocumentFee());
        loan.setServiceCharges(dto.getServiceCharges());
        loan.setDueDate(dto.getDueDate());
        loan.setRepaymentStartDate(dto.getRepaymentStartDate());

        // Fetch the CIF associated with the current account
        CurrentAccount currentAccount = currentAccountRepository.findById(loan.getCurrentAccount().getId())
                .orElseThrow(() -> new IllegalArgumentException("CurrentAccount not found with ID: " + loan.getCurrentAccount().getId()));
        CIF cif = currentAccount.getCif();
        if (cif == null) {
            throw new IllegalStateException("No CIF associated with the current account.");
        }

        // Fetch existing collaterals
        List<SmeLoanCollateral> existingCollaterals = smeLoanCollateralRepository.findBySmeLoanId(id);

        // Convert existing collaterals to DTOs for comparison
        List<SmeLoanCollateralDTO> existingCollateralDtos = existingCollaterals.stream()
                .map(coll -> {
                    SmeLoanCollateralDTO collDto = new SmeLoanCollateralDTO();
                    collDto.setCollateralId(coll.getCollateral().getId());
                    collDto.setCollateralAmount(coll.getCollateralAmount());
                    collDto.setDescription(coll.getCollateral().getDescription());
                    return collDto;
                })
                .collect(Collectors.toList());

        // Prepare updated collaterals
        List<SmeLoanCollateral> updatedCollaterals = new ArrayList<>();
        for (SmeLoanCollateralDTO collDto : dto.getCollaterals()) {
            if (collDto.getCollateralId() == null) {
                throw new IllegalArgumentException("Collateral ID is required.");
            }
            Collateral collateral = collateralRepository.findById(collDto.getCollateralId())
                    .orElseThrow(() -> new IllegalArgumentException("Collateral not found with ID: " + collDto.getCollateralId()));

            // Validate collateral belongs to the CIF
            if (!collateral.getCif().getId().equals(cif.getId())) {
                throw new IllegalArgumentException("Collateral ID " + collDto.getCollateralId() + " does not belong to the CIF associated with this loan.");
            }

            // Find matching existing collateral by collateralId
            SmeLoanCollateral existingColl = existingCollaterals.stream()
                    .filter(coll -> coll.getCollateral().getId().equals(collDto.getCollateralId()))
                    .findFirst()
                    .orElse(null);

            if (existingColl != null) {
                // Update existing collateral amount if changed
                existingColl.setCollateralAmount(collDto.getCollateralAmount());
                updatedCollaterals.add(existingColl);
            } else {
                // Add new collateral
                SmeLoanCollateral newColl = new SmeLoanCollateral();
                newColl.setCollateralAmount(collDto.getCollateralAmount());
                newColl.setCollateral(collateral);
                newColl.setSmeLoan(loan);
                updatedCollaterals.add(newColl);
            }
        }

        // Calculate total collateral amount
        BigDecimal totalCollateralAmount = updatedCollaterals.stream()
                .map(coll -> coll.getCollateralAmount() == null ? BigDecimal.ZERO : coll.getCollateralAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (loan.getLoanAmount().compareTo(totalCollateralAmount) > 0) {
            throw new IllegalArgumentException("Loan amount cannot exceed total collateral amount.");
        }

        // Identify collaterals to delete
        List<SmeLoanCollateral> collateralsToDelete = existingCollaterals.stream()
                .filter(existing -> updatedCollaterals.stream()
                        .noneMatch(updated -> updated.getCollateral().getId().equals(existing.getCollateral().getId())))
                .collect(Collectors.toList());
        smeLoanCollateralRepository.deleteAll(collateralsToDelete);

        // Save updated and new collaterals
        smeLoanCollateralRepository.saveAll(updatedCollaterals);

        // Save the updated loan
        SmeLoanRegistration updatedLoan = smeLoanRegistrationRepository.save(loan);
        return mapToDTO(updatedLoan.getId());
    }

    @Override
    public List<SmeLoanRegistrationDTO> getPendingLoans() {
        return smeLoanRegistrationRepository.findByStatus(3)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SmeLoanRegistrationDTO> getApprovedLoans() {
        return smeLoanRegistrationRepository.findByStatus(4) // Status 4 = Approved
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public SmeLoanRegistrationDTO approveLoan(Long id) {
        // Fetch the loan by ID
        SmeLoanRegistration loan = smeLoanRegistrationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found with ID: " + id));

        // Check if the loan status is pending (status 3)
        if (loan.getStatus() != 3) {
            throw new IllegalStateException("Only pending loans (status 3) can be approved.");
        }

        // Update the loan status to approved (status 4)
        loan.setStatus(4);
        SmeLoanRegistration updatedLoan = smeLoanRegistrationRepository.save(loan);

        // Generate the repayment schedule for the approved loan
        repaymentScheduleService.generateRepaymentSchedule(id);

        // Map the updated loan to DTO and return
        return mapToDTO(updatedLoan);
    }

    private SmeLoanRegistrationDTO mapToDTO(SmeLoanRegistration loan) {
        return mapToDTO(loan.getId());
    }

    private SmeLoanRegistrationDTO mapToDTO(Long loanId) {
        SmeLoanRegistration loan = smeLoanRegistrationRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found with ID: " + loanId));

        SmeLoanRegistrationDTO dto = new SmeLoanRegistrationDTO();
        dto.setId(loan.getId());
        dto.setLoanAmount(loan.getLoanAmount());
        dto.setInterestRate(loan.getInterestRate());
        dto.setGracePeriod(loan.getGracePeriod());
        dto.setRepaymentDuration((long) loan.getRepaymentDuration());
        dto.setDocumentFee(loan.getDocumentFee());
        dto.setServiceCharges(loan.getServiceCharges());
        dto.setStatus(loan.getStatus());
        dto.setDueDate(loan.getDueDate());
        dto.setRepaymentStartDate(loan.getRepaymentStartDate());


        Long currentAccountId = loan.getCurrentAccount().getId();
        CurrentAccount currentAccount = currentAccountRepository.findById(currentAccountId)
                .orElseThrow(() -> new IllegalArgumentException("CurrentAccount not found with ID: " + currentAccountId));
        dto.setCurrentAccountId(currentAccount.getId());
        dto.setAccountNumber(currentAccount.getAccountNumber());


        CIF cif = currentAccount.getCif();
        if (cif != null) {
            CIF finalCif = cif;
            cif = cifRepository.findById(cif.getId())
                    .orElseThrow(() -> new IllegalArgumentException("CIF not found with ID: " + finalCif.getId()));
            SmeLoanRegistrationDTO.CIFDTO cifDTO = new SmeLoanRegistrationDTO.CIFDTO();
            cifDTO.setId(cif.getId());
            cifDTO.setName(cif.getName());
            cifDTO.setSerialNumber(cif.getSerialNumber());
            cifDTO.setNrcNumber(cif.getNrcNumber());
            cifDTO.setEmail(cif.getEmail());
            dto.setCif(cifDTO);
        }

        List<SmeLoanCollateral> collaterals = smeLoanCollateralRepository.findBySmeLoanId(loanId);
        List<SmeLoanCollateralDTO> collateralDTOs = collaterals.stream()
                .map(coll -> {
                    Collateral collateral = collateralRepository.findById(coll.getCollateral().getId())
                            .orElseThrow(() -> new IllegalArgumentException("Collateral not found with ID: " + coll.getCollateral().getId()));
                    SmeLoanCollateralDTO collDTO = new SmeLoanCollateralDTO();
                    collDTO.setCollateralId(collateral.getId());
                    collDTO.setCollateralAmount(coll.getCollateralAmount());
                    collDTO.setDescription(coll.getCollateral().getDescription());
                    return collDTO;
                })
                .collect(Collectors.toList());
        dto.setCollaterals(collateralDTOs);

        BigDecimal totalCollateralAmount = collateralDTOs.stream()
                .map(SmeLoanCollateralDTO::getCollateralAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalCollateralAmount(totalCollateralAmount);

        return dto;
    }
}

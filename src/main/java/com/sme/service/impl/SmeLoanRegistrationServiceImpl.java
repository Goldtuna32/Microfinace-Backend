package com.sme.service.impl;

import com.sme.dto.LoanRegistrationRequest;
import com.sme.dto.SmeLoanCollateralDTO;
import com.sme.dto.SmeLoanRegistrationDTO;
import com.sme.entity.*;
import com.sme.exception.*;
import com.sme.repository.*;
import com.sme.service.RepaymentScheduleService;
import com.sme.service.SmeLoanRegistrationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        validateL oanRequest(request);

        SmeLoanRegistration loan = new SmeLoanRegistration();
        SmeLoanRegistrationDTO loanDTO = request.getLoan();

        loan.setLoanAmount(loanDTO.getLoanAmount());     
        loan.setInterestRate(loanDTO.getInterestRate());
        loan.setLate_fee_rate(loanDTO.getLate_fee_rate());
        loan.setNinety_day_late_fee_rate(loanDTO.getNinety_day_late_fee_rate());
        loan.setOne_hundred_and_eighty_late_fee_rate(loanDTO.getOne_hundred_and_eighty_day_late_fee_rate());
        loan.setGracePeriod(loanDTO.getGracePeriod());
        loan.setRepaymentDuration(loanDTO.getRepaymentDuration());
        loan.setDocumentFee(loanDTO.getDocumentFee());
        loan.setServiceCharges(loanDTO.getServiceCharges());
        loan.setStatus(loanDTO.getStatus() != null ? loanDTO.getStatus() : 3); // Default to pending (3)
        loan.setDueDate(loanDTO.getDueDate());
        loan.setRepaymentStartDate(loanDTO.getRepaymentStartDate());

        String serialCode = generateSerialCode(loanDTO.getCurrentAccountId());
        loan.setSerialCode(serialCode);

        // Validate if Current Account exists
        CurrentAccount currentAccount = currentAccountRepository.findById(loanDTO.getCurrentAccountId())
                .orElseThrow(() -> new CurrentAccountNotFoundException(loanDTO.getCurrentAccountId()));
        loan.setCurrentAccount(currentAccount);

        // Step 1: Save Loan First
        SmeLoanRegistration savedLoan = smeLoanRegistrationRepository.saveAndFlush(loan);

        // Step 2: Map Collaterals after Loan is saved
        List<SmeLoanCollateral> loanCollaterals = request.getCollaterals().stream()
                .map(dto -> {
                    Collateral collateral = collateralRepository.findById(dto.getCollateralId())
                            .orElseThrow(() -> new CollateralNotFoundException(dto.getCollateralId()));
                    SmeLoanCollateral coll = new SmeLoanCollateral();
                    coll.setCollateralAmount(dto.getCollateralAmount());
                    coll.setCollateral(collateral);
                    coll.setSmeLoan(savedLoan); // ✅ Use savedLoan with ID
                    return coll;
                })
                .collect(Collectors.toList());

        // Step 3: Save Collaterals
        smeLoanCollateralRepository.saveAll(loanCollaterals);

        return mapToDTO(savedLoan.getId());
    }



    private String generateSerialCode(Long currentAccountId) {
        CurrentAccount account = currentAccountRepository.findById(currentAccountId)
                .orElseThrow(() -> new CurrentAccountNotFoundException(currentAccountId));
        String accountNumber = account.getAccountNumber(); // Assume this field exists

        Long loanCount = smeLoanRegistrationRepository.countByCurrentAccountId(currentAccountId);
        Long nextNumber = loanCount + 1;
        String formattedNumber = String.format("%06d", nextNumber);

        return "SML-" + accountNumber + "-" + formattedNumber;
    }

    @Override
    @Transactional
    public SmeLoanRegistrationDTO updateLoan(Long id, SmeLoanRegistrationDTO dto) {
        SmeLoanRegistration loan = smeLoanRegistrationRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException(id));

        validateLoanDTO(dto);

        loan.setLoanAmount(dto.getLoanAmount());
        loan.setInterestRate(dto.getInterestRate());
        loan.setGracePeriod(dto.getGracePeriod());
        loan.setRepaymentDuration(dto.getRepaymentDuration());
        loan.setDocumentFee(dto.getDocumentFee());
        loan.setServiceCharges(dto.getServiceCharges());
        loan.setDueDate(dto.getDueDate());
        loan.setRepaymentStartDate(dto.getRepaymentStartDate());

        CurrentAccount currentAccount = currentAccountRepository.findById(loan.getCurrentAccount().getId())
                .orElseThrow(() -> new CurrentAccountNotFoundException(loan.getCurrentAccount().getId()));
        CIF cif = currentAccount.getCif();
        if (cif == null) {
            throw new LoanValidationException("No CIF associated with the current account.");
        }

        List<SmeLoanCollateral> existingCollaterals = smeLoanCollateralRepository.findBySmeLoanId(id);
        List<SmeLoanCollateral> updatedCollaterals = new ArrayList<>();

        for (SmeLoanCollateralDTO collDto : dto.getCollaterals()) {
            Collateral collateral = collateralRepository.findById(collDto.getCollateralId())
                    .orElseThrow(() -> new CollateralNotFoundException(collDto.getCollateralId()));

            if (!collateral.getCif().getId().equals(cif.getId())) {
                throw new CollateralMismatchException(collDto.getCollateralId(), cif.getId());
            }

            SmeLoanCollateral existingColl = existingCollaterals.stream()
                    .filter(coll -> coll.getCollateral().getId().equals(collDto.getCollateralId()))
                    .findFirst()
                    .orElse(null);

            if (existingColl != null) {
                existingColl.setCollateralAmount(collDto.getCollateralAmount());
                updatedCollaterals.add(existingColl);
            } else {
                SmeLoanCollateral newColl = new SmeLoanCollateral();
                newColl.setCollateralAmount(collDto.getCollateralAmount());
                newColl.setCollateral(collateral);
                newColl.setSmeLoan(loan);
                updatedCollaterals.add(newColl);
            }
        }

        BigDecimal totalCollateralAmount = updatedCollaterals.stream()
                .map(coll -> coll.getCollateralAmount() == null ? BigDecimal.ZERO : coll.getCollateralAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (loan.getLoanAmount().compareTo(totalCollateralAmount) > 0) {
            throw new InvalidLoanAmountException(
                    "Loan amount (" + loan.getLoanAmount() + ") cannot exceed total collateral amount (" + totalCollateralAmount + ")");
        }

        try {
            List<SmeLoanCollateral> collateralsToDelete = existingCollaterals.stream()
                    .filter(existing -> updatedCollaterals.stream()
                            .noneMatch(updated -> updated.getCollateral().getId().equals(existing.getCollateral().getId())))
                    .collect(Collectors.toList());
            smeLoanCollateralRepository.deleteAll(collateralsToDelete);

            smeLoanCollateralRepository.saveAll(updatedCollaterals);
            SmeLoanRegistration updatedLoan = smeLoanRegistrationRepository.save(loan);
            return mapToDTO(updatedLoan.getId());
        } catch (Exception e) {
            throw new LoanUpdateException(
                    "Failed to update loan with id: " + id, e);
        }
    }



    @Override
    public Page<SmeLoanRegistrationDTO> getAllApprovedLoans(Pageable pageable) {
        Page<SmeLoanRegistration> smeLoanRegistrationsPage = smeLoanRegistrationRepository.findAllPendingLoans(pageable);
        return smeLoanRegistrationsPage.map(this::mapToDTO);
    }

    @Override
    public List<SmeLoanRegistrationDTO> getPendingLoans() {
        return smeLoanRegistrationRepository.findByStatus(3)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<SmeLoanRegistrationDTO> getAllPendingLoans(Pageable pageable) {
        Page<SmeLoanRegistration> smeLoanRegistrationsPage = smeLoanRegistrationRepository.findAllPendingLoans(pageable);
        return smeLoanRegistrationsPage.map(this::mapToDTO);
    }


    @Override
    @Transactional
    public SmeLoanRegistrationDTO approveLoan(Long id) {
        SmeLoanRegistration loan = smeLoanRegistrationRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException(id));

        if (loan.getStatus() != 3) {
            throw new InvalidStatusTransitionException(loan.getStatus(), 4);
        }

        try {
            loan.setStatus(4);
            SmeLoanRegistration updatedLoan = smeLoanRegistrationRepository.save(loan);
            repaymentScheduleService.generateRepaymentSchedule(id);
            return mapToDTO(updatedLoan);
        } catch (Exception e) {
            throw new LoanUpdateException(
                    "Failed to approve loan with id: " + id, e);
        }
    }

    private SmeLoanRegistrationDTO mapToDTO(SmeLoanRegistration loan) {
        return mapToDTO(loan.getId());
    }

    private SmeLoanRegistrationDTO mapToDTO(Long loanId) {
        SmeLoanRegistration loan = smeLoanRegistrationRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException(loanId));

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

        CurrentAccount currentAccount = currentAccountRepository.findById(loan.getCurrentAccount().getId())
                .orElseThrow(() -> new CurrentAccountNotFoundException(loan.getCurrentAccount().getId()));
        dto.setCurrentAccountId(currentAccount.getId());
        dto.setAccountNumber(currentAccount.getAccountNumber());

        CIF cif = new CIF();
        CIF finalCif = cif;
        cif = cifRepository.findById(cif.getId())
                .orElseThrow(() -> new CIFNotFoundException(finalCif.getId()));
        SmeLoanRegistrationDTO.CIFDTO cifDTO = new SmeLoanRegistrationDTO.CIFDTO();
        cifDTO.setId(cif.getId());
        cifDTO.setName(cif.getName());
        cifDTO.setSerialNumber(cif.getSerialNumber());
        cifDTO.setNrcNumber(cif.getNrcNumber());
        cifDTO.setEmail(cif.getEmail());
        dto.setCif(cifDTO);

        List<SmeLoanCollateral> collaterals = smeLoanCollateralRepository.findBySmeLoanId(loanId);
        List<SmeLoanCollateralDTO> collateralDTOs = collaterals.stream()
                .map(coll -> {
                    Collateral collateral = collateralRepository.findById(coll.getCollateral().getId())
                            .orElseThrow(() -> new CollateralNotFoundException(coll.getCollateral().getId()));
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

    private void validateLoanRequest(LoanRegistrationRequest request) {
        if (request.getLoan() == null) {
            throw new MissingRequiredFieldException("loan");
        }
        validateLoanDTO(request.getLoan());

        if (request.getCollaterals() == null || request.getCollaterals().isEmpty()) {
            throw new LoanValidationException("At least one collateral is required.");
        }

        for (SmeLoanCollateralDTO coll : request.getCollaterals()) {
            if (coll.getCollateralId() == null) {
                throw new MissingRequiredFieldException("collateralId");
            }
            if (coll.getCollateralAmount() == null) {
                throw new MissingRequiredFieldException("collateralAmount");
            }
            if (coll.getCollateralAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidLoanAmountException(
                        "Collateral amount must be positive: " + coll.getCollateralAmount());
            }
        }
    }

    private void validateLoanDTO(SmeLoanRegistrationDTO dto) {
        if (dto.getCurrentAccountId() == null) {
            throw new MissingRequiredFieldException("currentAccountId");
        }
        if (dto.getLoanAmount() == null) {
            throw new MissingRequiredFieldException("loanAmount");
        }
        if (dto.getInterestRate() == null) {
            throw new MissingRequiredFieldException("interestRate");
        }
        if (dto.getRepaymentDuration() == null) {
            throw new MissingRequiredFieldException("repaymentDuration");
        }

        if (dto.getLoanAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidLoanAmountException("Loan amount must be positive: " + dto.getLoanAmount());
        }
        if (dto.getInterestRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidInterestRateException(dto.getInterestRate());
        }
        if (dto.getRepaymentDuration() <= 0) {
            throw new LoanValidationException("Repayment duration must be positive: " + dto.getRepaymentDuration());
        }

        if (dto.getStatus() != null && dto.getStatus() != 3 && dto.getStatus() != 4) {
            throw new LoanValidationException("Invalid initial status: " + dto.getStatus() + " (must be 3 or 4)");
        }
    }
}

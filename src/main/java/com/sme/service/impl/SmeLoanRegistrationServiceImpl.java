package com.sme.service.impl;

import com.sme.dto.*;
import com.sme.entity.*;
import com.sme.exception.*;
import com.sme.repository.*;
import com.sme.service.CIFService;
import com.sme.service.CurrentAccountService;
import com.sme.service.RepaymentScheduleService;
import com.sme.service.SmeLoanRegistrationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SmeLoanRegistrationServiceImpl implements SmeLoanRegistrationService {


    private final SmeLoanRegistrationRepository smeLoanRegistrationRepository;
    private final SmeLoanCollateralRepository smeLoanCollateralRepository;
    private final CollateralRepository collateralRepository;

    private final CurrentAccountService currentAccountService;
    private final CIFService cifService;

    private final ModelMapper modelMapper;


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
        validateLoanRequest(request);

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
        loan.setDueDate(LocalDateTime.now());

        String serialCode = generateSerialCode(loanDTO.getCurrentAccountId());
        loan.setSerialCode(serialCode);

        CurrentAccount currentAccount = currentAccountRepository.findById(loanDTO.getCurrentAccountId())
                .orElseThrow(() -> new CurrentAccountNotFoundException(loanDTO.getCurrentAccountId()));
        loan.setCurrentAccount(currentAccount);

        List<SmeLoanCollateral> loanCollaterals = request.getCollaterals().stream()
                .map(dto -> {
                    Collateral collateral = collateralRepository.findById(dto.getCollateralId())
                            .orElseThrow(() -> new CollateralNotFoundException(dto.getCollateralId()));
                    SmeLoanCollateral coll = new SmeLoanCollateral();
                    coll.setCollateralAmount(dto.getCollateralAmount());
                    coll.setCollateral(collateral);
                    return coll;
                })
                .collect(Collectors.toList());

        BigDecimal totalCollateralAmount = loanCollaterals.stream()
                .map(coll -> coll.getCollateralAmount() == null ? BigDecimal.ZERO : coll.getCollateralAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (loan.getLoanAmount().compareTo(totalCollateralAmount) > 0) {
            throw new InvalidLoanAmountException(
                    "Loan amount (" + loan.getLoanAmount() + ") cannot exceed total collateral amount (" + totalCollateralAmount + ")");
        }

        try {
            SmeLoanRegistration savedLoan = smeLoanRegistrationRepository.save(loan);
            for (SmeLoanCollateral coll : loanCollaterals) {
                coll.setSmeLoan(savedLoan);
                smeLoanCollateralRepository.save(coll);
            }
            return mapToDTO(savedLoan.getId());
        } catch (Exception e) {
            throw new LoanCreationException(
                    "Failed to register loan for current account ID: " + loanDTO.getCurrentAccountId(), e);
        }
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
        Page<SmeLoanRegistration> smeLoanRegistrationsPage = smeLoanRegistrationRepository.findAllActiveLoans(pageable);
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
            loan.setRepaymentStartDate(LocalDateTime.now());
            SmeLoanRegistration updatedLoan = smeLoanRegistrationRepository.save(loan);
            CurrentAccount currentAccount = loan.getCurrentAccount();
            if (currentAccount == null) {
                throw new CurrentAccountNotFoundException("Current account not found for loan ID: " + id);
            }
            BigDecimal loanAmount = loan.getLoanAmount(); // Assuming loanAmount is available in SmeLoanRegistration
            BigDecimal currentBalance = currentAccount.getBalance();
            currentAccount.setBalance(currentBalance.add(loanAmount));
            // Save the updated current account.
            currentAccountRepository.save(currentAccount);
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
        if (loanId == null) {
            throw new IllegalArgumentException("Loan ID must not be null");
        }

        SmeLoanRegistration loan = smeLoanRegistrationRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException(loanId));

        // Manual mapping instead of using ModelMapper
        SmeLoanRegistrationDTO dto = new SmeLoanRegistrationDTO();

        // Map basic fields
        dto.setId(loan.getId());
        dto.setSerialCode(loan.getSerialCode());
        dto.setLoanAmount(loan.getLoanAmount());
        dto.setInterestRate(loan.getInterestRate());
        dto.setLate_fee_rate(loan.getLate_fee_rate());
        dto.setNinety_day_late_fee_rate(loan.getNinety_day_late_fee_rate());
        dto.setOne_hundred_and_eighty_day_late_fee_rate(loan.getOne_hundred_and_eighty_late_fee_rate());
        dto.setGracePeriod(loan.getGracePeriod());
        dto.setRepaymentDuration(loan.getRepaymentDuration());
        dto.setDocumentFee(loan.getDocumentFee());
        dto.setServiceCharges(loan.getServiceCharges());
        dto.setStatus(loan.getStatus());
        dto.setDueDate(loan.getDueDate());
        dto.setRepaymentStartDate(loan.getRepaymentStartDate());

        // CurrentAccount handling
        if (loan.getCurrentAccount() == null) {
            throw new CurrentAccountNotFoundException("Current account is not associated with loan ID: " + loanId);
        }

        // Set current account ID directly
        dto.setCurrentAccountId(loan.getCurrentAccount().getId());
        dto.setAccountNumber(loan.getCurrentAccount().getAccountNumber());

        // Get full account details
        CurrentAccountDTO currentAccountDTO = currentAccountService.getAccountById(loan.getCurrentAccount().getId());
        dto.setCurrentAccountDetails(currentAccountDTO);

        // Get CIF details if available
        if (currentAccountDTO.getCifId() != null) {
            CIFDTO cifDTO = cifService.getCifById(currentAccountDTO.getCifId());
            dto.setCifDetails(cifDTO);

            // Also set the cif field for frontend compatibility
            dto.setCif(cifDTO); // Add this setter to your DTO
        }

        // Collateral handling
        List<SmeLoanCollateral> collaterals = smeLoanCollateralRepository.findBySmeLoanId(loanId);
        List<SmeLoanCollateralDTO> collateralDTOs = collaterals.stream()
                .map(coll -> {
                    if (coll.getCollateral() == null) {
                        throw new CollateralNotFoundException("Collateral is not associated with loan collateral ID: " + coll.getId());
                    }

                    // Manual mapping for collateral
                    SmeLoanCollateralDTO collDTO = new SmeLoanCollateralDTO();
                    collDTO.setId(coll.getId());
                    collDTO.setLoanId(coll.getSmeLoan() != null ? coll.getSmeLoan().getId() : null);
                    collDTO.setCollateralId(coll.getCollateral().getId());
                    collDTO.setCollateralAmount(coll.getCollateralAmount());
                    collDTO.setDescription(coll.getCollateral().getDescription());
                    collDTO.setCollateralCode(coll.getCollateral().getCollateralCode()); // Add this if needed
                    collDTO.setValue(coll.getCollateral().getValue()); // Add this if needed
                    collDTO.setStatus(coll.getCollateral().getStatus()); // Add this if needed

                    return collDTO;
                })
                .collect(Collectors.toList());
        dto.setCollaterals(collateralDTOs);

        // Calculate total collateral amount
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

    @Override
    @Transactional(readOnly = true)
    public SmeLoanRegistrationDTO getLoanDetailsById(Long loanId) {
        SmeLoanRegistration loan = smeLoanRegistrationRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with id: " + loanId));

        return mapToDTO(loanId); // Use the complete mapping method
    }

    @Override
    public long getTotalLoanCount() {
        return smeLoanRegistrationRepository.count();
    }

    @Override
    public long getPendingLoanCount() {
        return smeLoanRegistrationRepository.countByStatus(0); // Assuming 0 is pending status
    }

    @Override
    public long countByStatus(int status) {
        return smeLoanRegistrationRepository.countByStatus(status);
    }

    @Override
    public List<Object[]> countLoansByMonth() {
        return smeLoanRegistrationRepository.countLoansGroupedByMonth();
    }

    @Override
    public List<SmeLoanRegistration> findRecentLoans(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "repaymentStartDate"));
        return smeLoanRegistrationRepository.findAll(pageable).getContent();
    }

    @Override
    public List<SmeLoanRegistrationDTO> getAllPendingLoans(Long branchId) {
        List<SmeLoanRegistration> loans = smeLoanRegistrationRepository.findPendingLoans(branchId);
        return loans.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SmeLoanRegistrationDTO> getAllApprovedLoans(Long branchId) {
        List<SmeLoanRegistration> loans = smeLoanRegistrationRepository.findApprovedLoans(branchId);
        return loans.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private SmeLoanRegistrationDTO convertToDTO(SmeLoanRegistration loan) {
        SmeLoanRegistrationDTO dto = new SmeLoanRegistrationDTO();

        // Map basic fields
        dto.setId(loan.getId());
        dto.setSerialCode(loan.getSerialCode());
        dto.setLoanAmount(loan.getLoanAmount());
        dto.setInterestRate(loan.getInterestRate());
        dto.setLate_fee_rate(loan.getLate_fee_rate());
        dto.setNinety_day_late_fee_rate(loan.getNinety_day_late_fee_rate());
        dto.setOne_hundred_and_eighty_day_late_fee_rate(loan.getOne_hundred_and_eighty_late_fee_rate());
        dto.setGracePeriod(loan.getGracePeriod());
        dto.setRepaymentDuration(loan.getRepaymentDuration());
        dto.setDocumentFee(loan.getDocumentFee());
        dto.setServiceCharges(loan.getServiceCharges());
        dto.setStatus(loan.getStatus());
        dto.setDueDate(loan.getDueDate());
        dto.setRepaymentStartDate(loan.getRepaymentStartDate());

        // Handle CurrentAccount
        if (loan.getCurrentAccount() != null) {
            dto.setCurrentAccountId(loan.getCurrentAccount().getId());
            dto.setAccountNumber(loan.getCurrentAccount().getAccountNumber());

            // Map basic CIF info directly (avoid additional queries if possible)
            if (loan.getCurrentAccount().getCif() != null) {
                CIFDTO cifDTO = new CIFDTO();
                cifDTO.setId(loan.getCurrentAccount().getCif().getId());
                cifDTO.setSerialNumber(loan.getCurrentAccount().getCif().getSerialNumber());
                // Add other basic CIF fields as needed
                dto.setCifDetails(cifDTO);
                dto.setCif(cifDTO);
            }
        }

        // Handle Collaterals
        if (loan.getCollaterals() != null && !loan.getCollaterals().isEmpty()) {
            List<SmeLoanCollateralDTO> collateralDTOs = loan.getCollaterals().stream()
                    .map(coll -> {
                        SmeLoanCollateralDTO collDTO = new SmeLoanCollateralDTO();
                        collDTO.setId(coll.getId());
                        collDTO.setLoanId(loan.getId());
                        if (coll.getCollateral() != null) {
                            collDTO.setCollateralId(coll.getCollateral().getId());
                            collDTO.setCollateralAmount(coll.getCollateralAmount());
                            collDTO.setDescription(coll.getCollateral().getDescription());
                            // Add other collateral fields as needed
                        }
                        return collDTO;
                    })
                    .collect(Collectors.toList());
            dto.setCollaterals(collateralDTOs);

            // Calculate total collateral amount
            BigDecimal totalCollateralAmount = collateralDTOs.stream()
                    .map(SmeLoanCollateralDTO::getCollateralAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setTotalCollateralAmount(totalCollateralAmount);
        } else {
            dto.setCollaterals(Collections.emptyList());
            dto.setTotalCollateralAmount(BigDecimal.ZERO);
        }

        return dto;
    }
}

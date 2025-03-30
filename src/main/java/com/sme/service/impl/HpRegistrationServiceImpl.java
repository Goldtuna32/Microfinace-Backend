package com.sme.service.impl;

import com.sme.dto.HpRegistrationDTO;
import com.sme.entity.CurrentAccount;
import com.sme.entity.HpRegistration;
import com.sme.exception.InsufficientFundsException;
import com.sme.repository.CurrentAccountRepository;
import com.sme.repository.HpRegistrationRepository;
import com.sme.service.AccountTransactionService;
import com.sme.service.HpRegistrationService;
import com.sme.service.HpScheduleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HpRegistrationServiceImpl implements HpRegistrationService {

    @Autowired
    private HpRegistrationRepository repository;

    @Autowired
    private CurrentAccountRepository currentAccountRepository;  // Add this

    @Autowired
    private HpScheduleService hpScheduleService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AccountTransactionService accountTransactionService;

    @Override
    public List<HpRegistrationDTO> getAllHpRegistrations() {
        List<HpRegistration> hpRegistrations = repository.findAll();
        return hpRegistrations.stream()
                .map(hp -> modelMapper.map(hp, HpRegistrationDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public HpRegistrationDTO getHpRegistrationById(Long id) {
        HpRegistration hpRegistration = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hp Registration not found"));
        return modelMapper.map(hpRegistration, HpRegistrationDTO.class);
    }

    @Override
    @Transactional
    public HpRegistrationDTO createHpRegistration(HpRegistrationDTO dto) {
        // Generate the next HP number
        String hpNumber = generateNextHpNumber();
        dto.setHpNumber(hpNumber);

        // Set default start date to today if not provided
        if (dto.getStartDate() == null) {
            dto.setStartDate(LocalDate.now());
        }

        // Calculate end date based on loan term
        if (dto.getLoanTerm() != null && dto.getLoanTerm() > 0) {
            LocalDate endDate = calculateEndDate(dto.getStartDate(), dto.getLoanTerm());
            dto.setEndDate(endDate);
        }

        // Map and save the registration
        HpRegistration hpRegistration = modelMapper.map(dto, HpRegistration.class);
        hpRegistration.setOne_hundred_and_eighty_late_fee_rate(dto.getOne_hundred_and_eighty_late_fee_rate());
        hpRegistration.setCreatedDate(LocalDateTime.now());

        HpRegistration savedHpRegistration = repository.save(hpRegistration);

        // Generate schedule after saving (so we have an ID)
        hpScheduleService.generateHpRepaymentSchedule(savedHpRegistration.getId());

        return modelMapper.map(savedHpRegistration, HpRegistrationDTO.class);
    }

    private String generateNextHpNumber() {
        // 1. Find the maximum existing HP number
        Optional<HpRegistration> lastHpRegistration = repository.findTopByOrderByHpNumberDesc();

        // 2. If no records exist, start with HP-0001
        if (lastHpRegistration.isEmpty()) {
            return "HP-0001";
        }

        // 3. Extract the numeric part from the last HP number
        String lastHpNumber = lastHpRegistration.get().getHpNumber();
        String numericPart = lastHpNumber.substring(3); // Remove "HP-" prefix

        try {
            int lastNumber = Integer.parseInt(numericPart);
            // 4. Increment and format with leading zeros
            return String.format("HP-%04d", lastNumber + 1);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid HP number format in database");
        }
    }

    private LocalDate calculateEndDate(LocalDate startDate, Integer loanTermMonths) {
        return startDate.plusMonths(loanTermMonths);
    }

//    @Override
//    public HpRegistrationDTO approveHpRegistration(Long registrationId, BigDecimal remainingAmount) throws Exception {
//        // 1. Find the HP registration
//        HpRegistration hpRegistration = repository.findById(registrationId)
//                .orElseThrow(() -> new RuntimeException("HP Registration not found"));
//
//        // 2. Check if already approved
//        if (hpRegistration.getStatus() == 4) {
//            throw new IllegalStateException("HP Registration already approved");
//        }
//
//        // 3. Validate financial details
//        if (hpRegistration.getDownPayment() == null || hpRegistration.getLoanAmount() == null) {
//            throw new IllegalStateException("Financial details not properly configured");
//        }
//
//        // 4. Validate the remaining amount matches expected value
//        BigDecimal expectedRemaining = hpRegistration.getLoanAmount().subtract(hpRegistration.getDownPayment());
//        if (remainingAmount.compareTo(expectedRemaining) != 0) {
//            throw new IllegalArgumentException(
//                    String.format("Remaining amount doesn't match expected value. Expected: %s, Provided: %s",
//                            expectedRemaining, remainingAmount)
//            );
//        }
//
//        // 5. Get customer account and validate balance
//        CurrentAccount customerAccount = hpRegistration.getCurrentAccount();
//        if (customerAccount == null) {
//            throw new IllegalStateException("No current account assigned to HP registration");
//        }
//
//        if (customerAccount.getBalance().compareTo(hpRegistration.getDownPayment()) < 0) {
//            throw new InsufficientFundsException(
//                    String.format("Customer has insufficient funds. Required: %s, Available: %s",
//                            hpRegistration.getDownPayment(), customerAccount.getBalance())
//            );
//        }
//
//        // 6. Get dealer account
//        CurrentAccount dealerAccount = currentAccountRepository.findDealerAccountByHpProductId(hpRegistration.getHpProductId())
//                .orElseThrow(() -> new EntityNotFoundException(
//                        "Dealer account not found for HP product id: " + hpRegistration.getHpProductId()));
//
//
//        // 7. Process financial transactions
//        try {
//            // Transfer down payment from customer to dealer
//            accountTransactionService.transferFunds(
//                    customerAccount.getId(),
//                    dealerAccount.getId(),
//                    hpRegistration.getDownPayment(),
//                    "HP Down Payment - " + hpRegistration.getHpNumber()
//            );
//
//            // 8. Update HP registration status
//            hpRegistration.setStatus(4); // Approved
//            hpRegistration.setStartDate(LocalDateTime.now());
////
////            if (hpRegistration.getLoanTerm() != null) {
////                hpRegistration.setEndDate(hpRegistration.getStartDate().plusMonths(hpRegistration.getLoanTerm()));
////            }
//
//            HpRegistration approvedHp = repository.save(hpRegistration);
//
//            return modelMapper.map(approvedHp, HpRegistrationDTO.class);
//
//        } catch (Exception e) {
//            throw new Exception("Failed to process HP approval: " + e.getMessage(), e);
//        }
//    }

    @Override
    public HpRegistrationDTO updateHpRegistration(Long id, HpRegistrationDTO dto) {
        HpRegistration existingHp = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hp Registration not found"));

        if (dto.getHpNumber() != null) existingHp.setHpNumber(dto.getHpNumber());
        if (dto.getLoanAmount() != null) existingHp.setLoanAmount(dto.getLoanAmount());
        if (dto.getDownPayment() != null) existingHp.setDownPayment(dto.getDownPayment());
        if (dto.getLoanTerm() != null) existingHp.setLoanTerm(dto.getLoanTerm());
        if (dto.getInterestRate() != null) existingHp.setInterestRate(dto.getInterestRate());
        if (dto.getStartDate() != null) existingHp.setStartDate(dto.getStartDate());
        //if (dto.getEndDate() != null) existingHp.setEndDate(dto.getEndDate());
        if (dto.getStatus() != null) existingHp.setStatus(dto.getStatus());
        if (dto.getCurrentAccountId() != null) {
            CurrentAccount currentAccount = currentAccountRepository.findById(dto.getCurrentAccountId())
                    .orElseThrow(() -> new RuntimeException("Current Account not found"));
            existingHp.setCurrentAccount(currentAccount);
        }
        
        if (dto.getHpProductId() != null) existingHp.setHpProductId(dto.getHpProductId());

        HpRegistration updatedHp = repository.save(existingHp);
        return modelMapper.map(updatedHp, HpRegistrationDTO.class);
    }

    @Override
    public void deleteHpRegistration(Long id) {
        repository.deleteById(id);
    }

    @Override
    public long getTotalHpCount() {
        return repository.count();
    }

    @Override
    public long getActiveHpCount() {
        return repository.countByStatus(1); // Assuming 1 is active status
    }

    @Override
    public List<Object[]> countHpByMonth() {
        return repository.countHpRegistrationsGroupedByMonth();
    }

    @Override
    public List<HpRegistrationDTO> getAllPendingHP(Long branchId) {
        List<HpRegistration> hpRegistrations = repository.findPendingHP(branchId);
        return hpRegistrations.stream()
                .map(hpRegistration -> {
                    HpRegistrationDTO dto = new HpRegistrationDTO();
                    // Manual mapping
                    dto.setId(hpRegistration.getId());
                    dto.setHpNumber(hpRegistration.getHpNumber());
                    dto.setCreatedDate(hpRegistration.getCreatedDate());
                    dto.setLoanAmount(hpRegistration.getLoanAmount());
                    dto.setDownPayment(hpRegistration.getDownPayment());
                    dto.setLoanTerm(hpRegistration.getLoanTerm());
                    dto.setInterestRate(hpRegistration.getInterestRate());
                    dto.setStartDate(hpRegistration.getStartDate());
                    dto.setStatus(hpRegistration.getStatus());
                    dto.setGracePeriod(hpRegistration.getGracePeriod());
                    dto.setLate_fee_rate(hpRegistration.getLate_fee_rate());
                    dto.setNinety_day_late_fee_rate(hpRegistration.getNinety_day_late_fee_rate());
                    dto.setOne_hundred_and_eighty_late_fee_rate(hpRegistration.getOne_hundred_and_eighty_late_fee_rate());

                    // Map current account ID
                    if(hpRegistration.getCurrentAccount() != null) {
                        dto.setCurrentAccountId(hpRegistration.getCurrentAccount().getId());
                    }

                    dto.setHpProductId(hpRegistration.getHpProductId());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<HpRegistrationDTO> getAllApprovedHP(Long branchId) {
        List<HpRegistration> hpRegistrations = repository.findApprovedHP(branchId);
        return hpRegistrations.stream()
                .map(hpRegistration -> {
                    HpRegistrationDTO dto = new HpRegistrationDTO();
                    // Manual mapping
                    dto.setId(hpRegistration.getId());
                    dto.setHpNumber(hpRegistration.getHpNumber());
                    dto.setCreatedDate(hpRegistration.getCreatedDate());
                    dto.setLoanAmount(hpRegistration.getLoanAmount());
                    dto.setDownPayment(hpRegistration.getDownPayment());
                    dto.setLoanTerm(hpRegistration.getLoanTerm());
                    dto.setInterestRate(hpRegistration.getInterestRate());
                    dto.setStartDate(hpRegistration.getStartDate());
                    dto.setStatus(hpRegistration.getStatus());
                    dto.setGracePeriod(hpRegistration.getGracePeriod());
                    dto.setLate_fee_rate(hpRegistration.getLate_fee_rate());
                    dto.setNinety_day_late_fee_rate(hpRegistration.getNinety_day_late_fee_rate());
                    dto.setOne_hundred_and_eighty_late_fee_rate(hpRegistration.getOne_hundred_and_eighty_late_fee_rate());

                    // Map current account ID
                    if (hpRegistration.getCurrentAccount() != null) {
                        dto.setCurrentAccountId(hpRegistration.getCurrentAccount().getId());
                    }

                    dto.setHpProductId(hpRegistration.getHpProductId());

                    return dto;
                })
                .collect(Collectors.toList());
    }
}

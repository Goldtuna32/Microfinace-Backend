package com.sme.service.impl;

import com.sme.dto.HpRegistrationDTO;
import com.sme.entity.CurrentAccount;
import com.sme.entity.HpRegistration;
import com.sme.exception.CurrentAccountNotFoundException;
import com.sme.exception.InvalidStatusTransitionException;
import com.sme.exception.LoanNotFoundException;
import com.sme.exception.LoanUpdateException;
import com.sme.repository.CurrentAccountRepository;
import com.sme.repository.HpRegistrationRepository;
import com.sme.service.HpRegistrationService;
import com.sme.service.HpScheduleService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HpRegistrationServiceImpl implements HpRegistrationService {

    @Autowired
    private HpRegistrationRepository repository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    private HpScheduleService HpScheduleService;

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
    public HpRegistrationDTO createHpRegistration(HpRegistrationDTO dto) {
        HpRegistration hpRegistration = modelMapper.map(dto, HpRegistration.class);
        hpRegistration.setCreatedDate(java.time.LocalDateTime.now());
        hpRegistration.setStatus(3);
        HpRegistration savedHpRegistration = repository.save(hpRegistration);
        return modelMapper.map(savedHpRegistration, HpRegistrationDTO.class);
    }

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
        if (dto.getEndDate() != null) existingHp.setEndDate(dto.getEndDate());
        if (dto.getStatus() != null) existingHp.setStatus(dto.getStatus());
//        if (dto.getCurrentAccountId() != null) existingHp.setCurrentAccountId(dto.getCurrentAccountId());
//        if (dto.getHpProductId() != null) existingHp.setHpProductId(dto.getHpProductId());

        HpRegistration updatedHp = repository.save(existingHp);
        return modelMapper.map(updatedHp, HpRegistrationDTO.class);
    }

    @Override
    public void deleteHpRegistration(Long id) {
        repository.deleteById(id);
    }


    @Transactional
    @Override
    public HpRegistrationDTO approveLoan(Long id) {
        HpRegistration loan = repository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException(id));

        if (loan.getStatus() != 3) {
            throw new InvalidStatusTransitionException(loan.getStatus(), 4);
        }

        try {
            loan.setStatus(4);
            HpRegistration updatedLoan = repository.save(loan);

            CurrentAccount currentAccount = loan.getCurrentAccount();


            if (currentAccount == null) {
                throw new CurrentAccountNotFoundException("Current account not found for loan ID: " + id);
            }

            BigDecimal loanAmount = loan.getLoanAmount();
            BigDecimal currentBalance = currentAccount.getBalance();
            currentAccount.setBalance(currentBalance.add(loanAmount));

            currentAccountRepository.save(currentAccount);

            HpScheduleService.generateHpRepaymentSchedule(id);

            return mapToDTO(updatedLoan); // Use the updatedLoan object here.

        } catch (CurrentAccountNotFoundException | InvalidStatusTransitionException | LoanNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new LoanUpdateException("Failed to approve loan with id: " + id, e);
        }
    }

    private HpRegistrationDTO mapToDTO(HpRegistration loan) {
        // Implement your mapping logic here.
        // Example:
        HpRegistrationDTO dto = new HpRegistrationDTO();
        dto.setId(loan.getId());
        // ... map other fields from loan to dto ...
        return dto;
    }



}

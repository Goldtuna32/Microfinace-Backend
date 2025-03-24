package com.sme.service.impl;

import com.sme.dto.CurrentAccountDTO;
import com.sme.entity.Collateral;
import com.sme.entity.CurrentAccount;
import com.sme.entity.CIF;
import com.sme.repository.CurrentAccountRepository;
import com.sme.repository.CIFRepository;
import com.sme.service.CurrentAccountService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class CurrentAccountServiceImpl implements CurrentAccountService {

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    @Autowired
    private CIFRepository cifRepository;

    @Autowired
    private ModelMapper modelMapper;

    // ✅ Convert Entity → DTO
    private CurrentAccountDTO convertToDTO(CurrentAccount account) {
        CurrentAccountDTO dto = modelMapper.map(account, CurrentAccountDTO.class);
        dto.setCifId(account.getCif().getId());
        return dto;
    }

    // ✅ Convert DTO → Entity
    private CurrentAccount convertToEntity(CurrentAccountDTO dto) {
        CurrentAccount account = modelMapper.map(dto, CurrentAccount.class);
        Optional<CIF> cif = cifRepository.findById(dto.getCifId());
        cif.ifPresent(account::setCif);
        return account;
    }

    // ✅ Get all Current Accounts
    @Override
    public List<CurrentAccountDTO> getAllCurrentAccounts() {
        List<CurrentAccount> accounts = currentAccountRepository.findAll();
        return accounts.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // ✅ Get Current Account by ID
    @Override
    public Optional<CurrentAccountDTO> getCurrentAccountById(Long id) {
        Optional<CurrentAccount> account = currentAccountRepository.findById(id);
        return account.map(this::convertToDTO);
    }

    @Override
    @Transactional
    public CurrentAccountDTO createCurrentAccount(CurrentAccountDTO accountDTO) {
        if (accountDTO.getCifId() == null) {
            throw new IllegalArgumentException("CIF ID must not be null.");
        }

        CIF cif = cifRepository.findById(accountDTO.getCifId())
                .orElseThrow(() -> new RuntimeException("CIF not found with ID: " + accountDTO.getCifId()));

        CurrentAccount account = new CurrentAccount();
        account.setCif(cif);
        account.setBalance(BigDecimal.ZERO);
        account.setMinimumBalance(accountDTO.getMinimumBalance());
        account.setMaximumBalance(accountDTO.getMaximumBalance());
        account.setStatus(accountDTO.getStatus());
        account.setDateCreated(new Date());
        account.setHoldAmount(BigDecimal.ZERO);

        // Generate and set the account number
        String accountNumber = generateAccountNumber(cif.getBranch().getBranchCode());
        account.setAccountNumber(accountNumber);

        CurrentAccount savedAccount = currentAccountRepository.save(account);
        return convertToDTO(savedAccount);
    }

    @Transactional
    public String generateAccountNumber(String branchCode) {
        String lastAccountNumber = currentAccountRepository.findLastAccountNumberByBranchCode(branchCode);

        if (lastAccountNumber == null || lastAccountNumber.isEmpty()) {
            return "CA-" + branchCode + "-0001";
        }

        try {
            String[] parts = lastAccountNumber.split("-");
            if (parts.length != 4) {
                throw new IllegalArgumentException("Invalid account number format: " + lastAccountNumber);
            }

            int lastNumber = Integer.parseInt(parts[3]);
            int newNumber = lastNumber + 1;
            return "CA-" + branchCode + "-" + String.format("%04d", newNumber);
        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing account number: " + lastAccountNumber + ". Falling back to 0001.");
            return "CA-" + branchCode + "-0001";
        }
    }

    @Override
    public CurrentAccountDTO getCurrentAccountByCifId(Long cifId) {
        if (cifId == null) {
            throw new IllegalArgumentException("CIF ID cannot be null");
        }

        CurrentAccount currentAccount = currentAccountRepository.findByCifId(cifId)
                .orElseThrow(() -> new IllegalArgumentException("Current account not found for CIF ID: " + cifId));

        return modelMapper.map(currentAccount, CurrentAccountDTO.class);
    }

    @Override
    public CurrentAccountDTO updateCurrentAccount(Long id, CurrentAccountDTO accountDTO) {
        CurrentAccount existingAccount = currentAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Current Account not found with ID: " + id));

        // Only update maximumBalance and minimumBalance
        existingAccount.setMaximumBalance(accountDTO.getMaximumBalance());
        existingAccount.setMinimumBalance(accountDTO.getMinimumBalance());

        // Validate that minimumBalance is not greater than maximumBalance
        if (existingAccount.getMinimumBalance().compareTo(existingAccount.getMaximumBalance()) > 0) {
            throw new IllegalArgumentException("Minimum balance cannot be greater than maximum balance.");
        }

        CurrentAccount updatedAccount = currentAccountRepository.save(existingAccount);
        return convertToDTO(updatedAccount);
    }



    @Transactional
    @Override
    public boolean softDeleteCurrentAccount(Long id) {
        if (currentAccountRepository.existsById(id)) {
            CurrentAccount currentAccount = currentAccountRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Current Account not found with ID: " + id));
            currentAccount.setStatus(2);
            currentAccountRepository.save(currentAccount);
            return true;
        }
        return false;
    }

    @Transactional
    @Override
    public boolean restoreCurrentAccount(Long id) {
        if (currentAccountRepository.existsById(id)) {
            CurrentAccount currentAccount = currentAccountRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Current Account not found with ID: " + id));
            currentAccount.setStatus(1); // Set status to 1 (active)
            currentAccountRepository.save(currentAccount);
            return true;
        }
        return false;
    }



    public boolean hasCurrentAccount(Long cifId) {
        return currentAccountRepository.existsByCifId(cifId);
    }

    @Override
    public Page<CurrentAccountDTO> getAllCurrentAccountsPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<CurrentAccount> accountPage = currentAccountRepository.findAll(pageable);
        return accountPage.map(this::convertToDTO);
    }
}

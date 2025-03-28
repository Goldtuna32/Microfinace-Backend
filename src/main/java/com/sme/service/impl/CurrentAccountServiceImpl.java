package com.sme.service.impl;

import com.sme.dto.CurrentAccountDTO;
import com.sme.entity.Collateral;
import com.sme.entity.CurrentAccount;
import com.sme.entity.CIF;
import com.sme.exception.*;
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

    @Override
    @Transactional
    public Optional<CurrentAccountDTO> getCurrentAccountById(Long id) {
        Optional<CurrentAccount> account = currentAccountRepository.findById(id);
        if (account.isEmpty()) {
            throw new CurrentAccountNotFoundException(id);
        }
        return account.map(this::convertToDTO);
    }

    @Override
    @Transactional
    public CurrentAccountDTO createCurrentAccount(CurrentAccountDTO accountDTO) {
        validateCurrentAccountDTO(accountDTO);

        CIF cif = cifRepository.findById(accountDTO.getCifId())
                .orElseThrow(() -> new CIFNotFoundException(accountDTO.getCifId()));

        try {
            CurrentAccount account = new CurrentAccount();
            account.setCif(cif);
            account.setBalance(BigDecimal.ZERO);
            account.setMinimumBalance(accountDTO.getMinimumBalance());
            account.setMaximumBalance(accountDTO.getMaximumBalance());
            account.setStatus(1); // Default to active
            account.setDateCreated(new Date());
            account.setHoldAmount(BigDecimal.ZERO);

            String accountNumber = generateAccountNumber(cif.getBranch().getBranchCode());
            account.setAccountNumber(accountNumber);

            CurrentAccount savedAccount = currentAccountRepository.save(account);
            return convertToDTO(savedAccount);
        } catch (Exception e) {
            throw new CurrentAccountCreationException(
                    "Failed to create current account for CIF ID: " + accountDTO.getCifId(), e);
        }
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
                throw new InvalidAccountNumberException(lastAccountNumber);
            }

            int lastNumber = Integer.parseInt(parts[3]);
            int newNumber = lastNumber + 1;
            return "CA-" + branchCode + "-" + String.format("%04d", newNumber);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new InvalidAccountNumberException(
                    "Error parsing account number: " + lastAccountNumber, e);
        }
    }

    @Override
    @Transactional
    public CurrentAccountDTO getCurrentAccountByCifId(Long cifId) {
        if (cifId == null) {
            throw new MissingRequiredFieldException("cifId");
        }

        CurrentAccount currentAccount = currentAccountRepository.findByCifId(cifId)
                .orElseThrow(() -> new CurrentAccountNotFoundException(
                        "Current account not found for CIF ID: " + cifId));
        return modelMapper.map(currentAccount, CurrentAccountDTO.class);
    }

    @Override
    @Transactional
    public CurrentAccountDTO updateCurrentAccount(Long id, CurrentAccountDTO accountDTO) {
        CurrentAccount existingAccount = currentAccountRepository.findById(id)
                .orElseThrow(() -> new CurrentAccountNotFoundException(id));

        validateUpdateCurrentAccountDTO(accountDTO);

        try {
            existingAccount.setMaximumBalance(accountDTO.getMaximumBalance());
            existingAccount.setMinimumBalance(accountDTO.getMinimumBalance());

            CurrentAccount updatedAccount = currentAccountRepository.save(existingAccount);
            return convertToDTO(updatedAccount);
        } catch (Exception e) {
            throw new CurrentAccountUpdateException(
                    "Failed to update current account with id: " + id, e);
        }
    }

    @Transactional
    @Override
    public boolean softDeleteCurrentAccount(Long id) {
        if (!currentAccountRepository.existsById(id)) {
            throw new CurrentAccountNotFoundException(id);
        }
        try {
            CurrentAccount currentAccount = currentAccountRepository.findById(id).get();
            currentAccount.setStatus(2);
            currentAccountRepository.save(currentAccount);
            return true;
        } catch (Exception e) {
            throw new CurrentAccountUpdateException(
                    "Failed to soft delete current account with id: " + id, e);
        }
    }

    @Transactional
    @Override
    public boolean restoreCurrentAccount(Long id) {
        if (!currentAccountRepository.existsById(id)) {
            throw new CurrentAccountNotFoundException(id);
        }
        try {
            CurrentAccount currentAccount = currentAccountRepository.findById(id).get();
            currentAccount.setStatus(1);
            currentAccountRepository.save(currentAccount);
            return true;
        } catch (Exception e) {
            throw new CurrentAccountUpdateException(
                    "Failed to restore current account with id: " + id, e);
        }
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

    private void validateCurrentAccountDTO(CurrentAccountDTO accountDTO) {
        if (accountDTO.getCifId() == null) {
            throw new MissingRequiredFieldException("cifId");
        }
        if (accountDTO.getMinimumBalance() == null) {
            throw new MissingRequiredFieldException("minimumBalance");
        }
        if (accountDTO.getMaximumBalance() == null) {
            throw new MissingRequiredFieldException("maximumBalance");
        }

        if (accountDTO.getMinimumBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidBalanceValueException("minimumBalance", accountDTO.getMinimumBalance());
        }
        if (accountDTO.getMaximumBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidBalanceValueException("maximumBalance", accountDTO.getMaximumBalance());
        }
        if (accountDTO.getMinimumBalance().compareTo(accountDTO.getMaximumBalance()) > 0) {
            throw new InvalidBalanceRangeException(accountDTO.getMinimumBalance(), accountDTO.getMaximumBalance());
        }
    }

    private void validateUpdateCurrentAccountDTO(CurrentAccountDTO accountDTO) {
        if (accountDTO.getMinimumBalance() == null) {
            throw new MissingRequiredFieldException("minimumBalance");
        }
        if (accountDTO.getMaximumBalance() == null) {
            throw new MissingRequiredFieldException("maximumBalance");
        }

        if (accountDTO.getMinimumBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidBalanceValueException("minimumBalance", accountDTO.getMinimumBalance());
        }
        if (accountDTO.getMaximumBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidBalanceValueException("maximumBalance", accountDTO.getMaximumBalance());
        }
        if (accountDTO.getMinimumBalance().compareTo(accountDTO.getMaximumBalance()) > 0) {
            throw new InvalidBalanceRangeException(accountDTO.getMinimumBalance(), accountDTO.getMaximumBalance());
        }
    }

    @Override
    public CurrentAccountDTO getAccountById(Long accountId) throws CurrentAccountNotFoundException {
        CurrentAccount account = currentAccountRepository.findById(accountId)
                .orElseThrow(() -> new CurrentAccountNotFoundException(accountId));

        CurrentAccountDTO dto = modelMapper.map(account, CurrentAccountDTO.class);

        // Map additional fields if needed
        if (account.getCif() != null) {
            dto.setCifId(account.getCif().getId());
        }

        return dto;
    }
}

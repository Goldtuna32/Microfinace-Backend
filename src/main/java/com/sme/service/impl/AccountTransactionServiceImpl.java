package com.sme.service.impl;

import com.sme.dto.AccountTransactionDTO;
import com.sme.entity.AccountTransaction;
import com.sme.entity.CurrentAccount;
import com.sme.entity.TransactionType;
import com.sme.exception.*;
import com.sme.repository.AccountTransactionRepository;
import com.sme.repository.CurrentAccountRepository;
import com.sme.service.AccountTransactionService;
import jakarta.transaction.Transaction;
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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountTransactionServiceImpl implements AccountTransactionService {

    @Autowired
    private AccountTransactionRepository transactionRepository;

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public AccountTransaction createTransaction(AccountTransactionDTO transactionDTO) {
        validateTransactionDTO(transactionDTO);

        CurrentAccount account = currentAccountRepository.findById(transactionDTO.getCurrentAccountId())
                .orElseThrow(() -> new CurrentAccountNotFoundException(transactionDTO.getCurrentAccountId()));

        if (account.getStatus() == 2) {
            throw new InactiveAccountException(account.getId());
        }

        BigDecimal newBalance;
        BigDecimal currentBalance = account.getBalance();
        BigDecimal transactionAmount = transactionDTO.getAmount();

        try {
            if (TransactionType.CREDIT.name().equals(transactionDTO.getTransactionType())) {
                newBalance = currentBalance.add(transactionAmount);
                if (newBalance.compareTo(account.getMaximumBalance()) > 0) {
                    throw new BalanceLimitExceededException(newBalance, account.getMaximumBalance());
                }
            } else if (TransactionType.DEBIT.name().equals(transactionDTO.getTransactionType())) {
                newBalance = currentBalance.subtract(transactionAmount);
                if (newBalance.compareTo(account.getMinimumBalance()) < 0) {
                    throw new InsufficientFundsException(newBalance, account.getMinimumBalance());
                }
            } else {
                throw new InvalidTransactionTypeException(transactionDTO.getTransactionType());
            }

            account.setBalance(newBalance);
            currentAccountRepository.save(account);

            AccountTransaction transaction = modelMapper.map(transactionDTO, AccountTransaction.class);
            transaction.setCurrentAccount(account);
            transaction.setTransactionDate(new Date());
            transaction.setStatus(transactionDTO.getStatus() != null ? transactionDTO.getStatus() : 6); // Default to 1 (e.g., Pending)

            return transactionRepository.save(transaction);
        } catch (Exception e) {
            throw new TransactionCreationException(
                    "Failed to create transaction for account ID: " + transactionDTO.getCurrentAccountId(), e);
        }
    }


    @Override
    public List<AccountTransactionDTO> getTransactionsByCurrentAccount(Long accountId) {
        if (accountId == null) {
            throw new MissingRequiredFieldException("currentAccountId");
        }

        List<AccountTransaction> transactions = transactionRepository.findByCurrentAccountId(accountId);
        return transactions.stream()
                .map(transaction -> modelMapper.map(transaction, AccountTransactionDTO.class))
                .collect(Collectors.toList());
    }


    private void validateTransactionDTO(AccountTransactionDTO transactionDTO) {
        if (transactionDTO.getCurrentAccountId() == null) {
            throw new MissingRequiredFieldException("currentAccountId");
        }
        if (transactionDTO.getAmount() == null) {
            throw new MissingRequiredFieldException("amount");
        }
        if (transactionDTO.getTransactionType() == null) {
            throw new MissingRequiredFieldException("transactionType");
        }

        if (transactionDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransactionValidationException(
                    "Transaction amount must be positive: " + transactionDTO.getAmount());
        }

        if (transactionDTO.getStatus() != null &&
                transactionDTO.getStatus() != 1 &&
                transactionDTO.getStatus() != 2) {
            throw new TransactionValidationException(
                    "Invalid transaction status: " + transactionDTO.getStatus() + " (must be 1 or 2)");
        }
    }
}




//    @Override
//
//    public Page<AccountTransaction> getTransactionsByCurrentAccount(
//            Long currentAccountId,
//            int page,
//            int size,
//            String sortBy,
//            String sortDir,
//            Date startDate,
//            Date endDate) {
//
//        // Default sorting order
//        Sort sort = sortDir.equalsIgnoreCase("desc")
//                ? Sort.by(sortBy).descending()
//                : Sort.by(sortBy).ascending();
//
//        Pageable pageable = PageRequest.of(page, size, sort);
//
//        // Handle null dates (default: Last 30 days)
//        if (startDate == null) {
//            startDate = new Date(System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)); // 30 days ago
//        }
//        if (endDate == null) {
//            endDate = new Date();
//        }
//
//        // Fetch paginated transactions
//        return transactionRepository.findByCurrentAccountIdAndTransactionDateBetween(
//                currentAccountId, startDate, endDate, pageable);
//    }



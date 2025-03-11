package com.sme.service.impl;

import com.sme.dto.AccountTransactionDTO;
import com.sme.entity.AccountTransaction;
import com.sme.entity.CurrentAccount;
import com.sme.entity.TransactionType;
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

    @Transactional
    public AccountTransaction createTransaction(AccountTransactionDTO transactionDTO) {
        CurrentAccount account = currentAccountRepository.findById(transactionDTO.getCurrentAccountId())
                .orElseThrow(() -> new RuntimeException("Current Account not found"));

        BigDecimal newBalance;
        BigDecimal currentBalance = account.getBalance();
        BigDecimal transactionAmount = transactionDTO.getAmount();

        if (TransactionType.CREDIT.name().equals(transactionDTO.getTransactionType())) {
            newBalance = currentBalance.add(transactionAmount);

            if (newBalance.compareTo(account.getMaximumBalance()) > 0) {
                throw new RuntimeException("Transaction exceeds the maximum allowed balance.");
            }
        } else if (TransactionType.DEBIT.name().equals(transactionDTO.getTransactionType())) {
            newBalance = currentBalance.subtract(transactionAmount);

            if (newBalance.compareTo(account.getMinimumBalance()) < 0) {
                throw new RuntimeException("Insufficient funds. Minimum balance requirement not met.");
            }
        } else {
            throw new RuntimeException("Invalid transaction type.");
        }

        // Update Current Account balance
        account.setBalance(newBalance);
        currentAccountRepository.save(account);

        // Save transaction
        AccountTransaction transaction = modelMapper.map(transactionDTO, AccountTransaction.class);
        transaction.setCurrentAccount(account);
        transaction.setTransactionDate(new Date());

        // Ensure status is set
        if (transactionDTO.getStatus() == null) {
            transaction.setStatus(1); // Default status (e.g., 1 = "Pending")
        } else {
            transaction.setStatus(transactionDTO.getStatus());
        }

        return transactionRepository.save(transaction);
    }


    @Override
    public List<AccountTransactionDTO> getTransactionsByCurrentAccount(Long accountId) {
        List<AccountTransaction> transactions = transactionRepository.findByCurrentAccountId(accountId);

        return transactions.stream()
                .map(transaction -> {
                    AccountTransactionDTO dto = modelMapper.map(transaction, AccountTransactionDTO.class);

                    return dto;
                })
                .collect(Collectors.toList());
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



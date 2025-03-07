package com.sme.service;

import com.sme.dto.AccountTransactionDTO;
import com.sme.entity.AccountTransaction;
import jakarta.transaction.Transaction;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.Date;

public interface AccountTransactionService {
    AccountTransaction createTransaction(AccountTransactionDTO transactionDTO);

    Page<AccountTransaction> getTransactionsByCurrentAccount(
            Long currentAccountId,
            int page,
            int size,
            String sortBy,
            String sortDir);
}

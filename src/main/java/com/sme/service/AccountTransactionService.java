package com.sme.service;

import com.sme.dto.AccountTransactionDTO;
import com.sme.entity.AccountTransaction;
import jakarta.transaction.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


public interface AccountTransactionService {
    AccountTransaction createTransaction(AccountTransactionDTO transactionDTO);

    List<AccountTransactionDTO> getTransactionsByCurrentAccount(Long accountId);
}

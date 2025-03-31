package com.sme.service.impl;

import com.sme.entity.RepaymentTransaction;
import com.sme.repository.RepaymentTransactionRepository;
import com.sme.service.RepaymentTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepaymentTransactionServiceImpl implements RepaymentTransactionService {

    private final RepaymentTransactionRepository transactionRepository;

    @Autowired
    public RepaymentTransactionServiceImpl(RepaymentTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public RepaymentTransaction createTransaction(RepaymentTransaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Override
    public List<RepaymentTransaction> getTransactionsByScheduleId(Long scheduleId) {
        return transactionRepository.findByRepaymentScheduleId(scheduleId);
    }

    @Override
    public List<RepaymentTransaction> getTransactionsByAccountId(Long accountId) {
        return transactionRepository.findByCurrentAccountId(accountId);
    }

    @Override
    public RepaymentTransaction getTransactionById(Long id) {
        return transactionRepository.findById(id).orElse(null);
    }
}
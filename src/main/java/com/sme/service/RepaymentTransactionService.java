package com.sme.service;

import com.sme.entity.RepaymentTransaction;
import java.util.List;

public interface RepaymentTransactionService {
    RepaymentTransaction createTransaction(RepaymentTransaction transaction);
    List<RepaymentTransaction> getTransactionsByScheduleId(Long scheduleId);
    List<RepaymentTransaction> getTransactionsByAccountId(Long accountId);
    RepaymentTransaction getTransactionById(Long id);
}

package com.sme.service.impl;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AutoPaymentOrchestrator {
    private final LoanAutoPayment loanPayment;
    private final HirePurchaseAutoPayment hpPayment;
    
    public AutoPaymentOrchestrator(
            LoanAutoPayment loanPayment,
            HirePurchaseAutoPayment hpPayment) {
        this.loanPayment = loanPayment;
        this.hpPayment = hpPayment;
    }
    
    @Scheduled(cron = "0 * * * * *")
    public void processAllPayments() {
        loanPayment.processPayments();
        hpPayment.processPayments();
    }
}

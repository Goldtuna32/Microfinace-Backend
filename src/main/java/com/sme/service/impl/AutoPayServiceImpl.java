// package com.sme.service.impl;

// import com.sme.service.AutoPaymentService;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Service;

// @Service
// public class AutoPayServiceImpl implements AutoPaymentService {
//     private final LoanAutoPayment loanStrategy;
//     private final HirePurchaseAutoPayment hpStrategy;

//     public AutoPayServiceImpl(
//             LoanAutoPayment loanStrategy,
//             HirePurchaseAutoPayment hpStrategy) {
//         this.loanStrategy = loanStrategy;
//         this.hpStrategy = hpStrategy;
//     }

//     @Scheduled(cron = "0 * * * * *")
//     @Override
//     public void processAutoPayments() {
//         loanStrategy.processAutoPayments();
//         hpStrategy.processAutoPayments();
//     }
// }
package com.sme.controller;

import com.sme.entity.RepaymentTransaction;
import com.sme.service.RepaymentTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repayment-transactions")
public class RepaymentTransactionController {

    private final RepaymentTransactionService transactionService;

    @Autowired
    public RepaymentTransactionController(RepaymentTransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<RepaymentTransaction> createTransaction(@RequestBody RepaymentTransaction transaction) {
        return ResponseEntity.ok(transactionService.createTransaction(transaction));
    }

    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<List<RepaymentTransaction>> getTransactionsByScheduleId(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(transactionService.getTransactionsByScheduleId(scheduleId));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<RepaymentTransaction>> getTransactionsByAccountId(@PathVariable Long accountId) {
        return ResponseEntity.ok(transactionService.getTransactionsByAccountId(accountId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepaymentTransaction> getTransactionById(@PathVariable Long id) {
        RepaymentTransaction transaction = transactionService.getTransactionById(id);
        return transaction != null ? ResponseEntity.ok(transaction) : ResponseEntity.notFound().build();
    }
}
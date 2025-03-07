package com.sme.controller;

import com.sme.dto.AccountTransactionDTO;
import com.sme.entity.AccountTransaction;
import com.sme.service.AccountTransactionService;
import jakarta.transaction.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;



@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:4200") // Allow Angular frontend to access API
public class AccountTransactionController {

    @Autowired
    private AccountTransactionService transactionService;

    @PostMapping
    public ResponseEntity<?> createTransaction(@RequestBody AccountTransactionDTO transactionDTO) {
        try {
            AccountTransaction transaction = transactionService.createTransaction(transactionDTO);
            return ResponseEntity.ok(transaction);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @GetMapping("/current-account/{accountId}")
    public ResponseEntity<Page<AccountTransaction>> getTransactionsByCurrentAccount(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Page<AccountTransaction> transactions = transactionService.getTransactionsByCurrentAccount(
                accountId, page, size, sortBy, sortDir);

        return ResponseEntity.ok(transactions);
    }

//    @GetMapping("/current-account/{accountId}")
//    public ResponseEntity<Page<AccountTransaction>> getTransactionsByCurrentAccount(
//            @PathVariable Long accountId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(defaultValue = "transactionDate") String sortBy,
//            @RequestParam(defaultValue = "desc") String sortDir,
//            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
//            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate) {
//
//        Page<AccountTransaction> transactions = transactionService.getTransactionsByCurrentAccount(
//                accountId, page, size, sortBy, sortDir, startDate, endDate);
//
//        return ResponseEntity.ok(transactions);
//    }
}

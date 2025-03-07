package com.sme.controller;

import com.sme.dto.LoanRegistrationRequest;
import com.sme.dto.SmeLoanRegistrationDTO;
import com.sme.entity.SmeLoanCollateral;
import com.sme.entity.SmeLoanRegistration;
import com.sme.service.SmeLoanRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class SmeLoanRegistrationController {

    @Autowired
    private SmeLoanRegistrationService loanService;

    @PostMapping("/register")
    public ResponseEntity<SmeLoanRegistrationDTO> registerLoan(@RequestBody LoanRegistrationRequest request) {
        System.out.println("request = " + request);
        SmeLoanRegistrationDTO savedLoan = loanService.registerLoan(request);
        return ResponseEntity.ok(savedLoan);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SmeLoanRegistrationDTO> getLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<SmeLoanRegistrationDTO>> getPendingLoans() {
        return ResponseEntity.ok(loanService.getPendingLoans());
    }

    @GetMapping("/approved")
    public ResponseEntity<List<SmeLoanRegistrationDTO>> getApprovedLoans() {
        return ResponseEntity.ok(loanService.getApprovedLoans());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<SmeLoanRegistrationDTO> approveLoan(@PathVariable Long id) {
        SmeLoanRegistrationDTO approvedLoan = loanService.approveLoan(id);
        return ResponseEntity.ok(approvedLoan);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SmeLoanRegistrationDTO> updateLoan(@PathVariable Long id, @RequestBody SmeLoanRegistrationDTO dto) {
        SmeLoanRegistrationDTO updatedLoan = loanService.updateLoan(id, dto);
        return ResponseEntity.ok(updatedLoan);
    }


}

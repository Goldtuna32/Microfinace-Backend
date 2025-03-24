package com.sme.controller;

import com.sme.dto.CIFDTO;
import com.sme.dto.LoanRegistrationRequest;
import com.sme.dto.SmeLoanRegistrationDTO;
import com.sme.entity.SmeLoanCollateral;
import com.sme.entity.SmeLoanRegistration;
import com.sme.repository.SmeLoanRegistrationRepository;
import com.sme.service.SmeLoanRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class SmeLoanRegistrationController {

    @Autowired
    private SmeLoanRegistrationService loanService;


    @PreAuthorize("hasRole('SME_LOAN_REGISTRATION_CREATE')")
    @PostMapping("/register")
    public ResponseEntity<SmeLoanRegistrationDTO> registerLoan(@RequestBody LoanRegistrationRequest request) {
        System.out.println("request = " + request);
        SmeLoanRegistrationDTO savedLoan = loanService.registerLoan(request);
        return ResponseEntity.ok(savedLoan);
    }


    @PreAuthorize("hasRole('SME_LOAN_REGISTRATION_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<SmeLoanRegistrationDTO> getLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }

    @PreAuthorize("hasAuthority('SME_LOAN_REGISTRATION_READ')")
    @GetMapping("/pending")
    public ResponseEntity<Page<SmeLoanRegistrationDTO>> getAllPendingLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SmeLoanRegistrationDTO> smeLoanRegistrationDTOPage = loanService.getAllPendingLoans(pageable);
        return ResponseEntity.ok(smeLoanRegistrationDTOPage);
    }

    @PreAuthorize("hasAuthority('SME_LOAN_REGISTRATION_READ')")
    @GetMapping("/approved")
    public ResponseEntity<Page<SmeLoanRegistrationDTO>> getAllApprovedLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SmeLoanRegistrationDTO> smeLoanRegistrationDTOPage = loanService.getAllApprovedLoans(pageable);
        return ResponseEntity.ok(smeLoanRegistrationDTOPage);
    }

    @PreAuthorize("hasRole('SME_LOAN_REGISTRATION_READ')")
    @PostMapping("/{id}/approve")
    public ResponseEntity<SmeLoanRegistrationDTO> approveLoan(@PathVariable Long id) {
        SmeLoanRegistrationDTO approvedLoan = loanService.approveLoan(id);
        return ResponseEntity.ok(approvedLoan);
    }

    @PreAuthorize("hasRole('SME_LOAN_REGISTRATION_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<SmeLoanRegistrationDTO> updateLoan(@PathVariable Long id, @RequestBody SmeLoanRegistrationDTO dto) {
        SmeLoanRegistrationDTO updatedLoan = loanService.updateLoan(id, dto);
        return ResponseEntity.ok(updatedLoan);
    }


}

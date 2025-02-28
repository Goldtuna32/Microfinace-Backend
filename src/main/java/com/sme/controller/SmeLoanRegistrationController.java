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
    public ResponseEntity<SmeLoanRegistration> registerLoan(
            @RequestBody LoanRegistrationRequest request) {

        SmeLoanRegistration loan = request.getLoan();
        List<SmeLoanCollateral> collaterals = request.getCollaterals();

        SmeLoanRegistration savedLoan = loanService.registerLoan(loan, collaterals);

        return ResponseEntity.ok(savedLoan);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SmeLoanRegistrationDTO> getLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }


}

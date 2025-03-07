package com.sme.controller;

import com.sme.dto.SmeLoanCollateralDTO;
import com.sme.service.SmeLoanCollateralService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan-collaterals")
@RequiredArgsConstructor
public class SmeLoanCollateralController {
    @Autowired
    private SmeLoanCollateralService loanCollateralService;

    //private final SmeLoanCollateralService loanCollateralService;

    @PostMapping
    public ResponseEntity<SmeLoanCollateralDTO> linkCollateralToLoan(@RequestBody SmeLoanCollateralDTO dto) {
        return ResponseEntity.ok(loanCollateralService.linkCollateralToLoan(dto));
    }

//    @GetMapping("/loan/{loanId}")
//    public ResponseEntity<List<SmeLoanCollateralDTO>> getCollateralsForLoan(@PathVariable Long loanId) {
//        return ResponseEntity.ok(loanCollateralService.getCollateralsForLoan(loanId));
//    }
//
//    @GetMapping("/collateral/{collateralId}")
//    public ResponseEntity<List<SmeLoanCollateralDTO>> getLoansForCollateral(@PathVariable Long collateralId) {
//        return ResponseEntity.ok(loanCollateralService.getLoansForCollateral(collateralId));
//    }
}

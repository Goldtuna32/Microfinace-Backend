package com.sme.controller;

import com.sme.dto.RepaymentScheduleDTO;
import com.sme.service.RepaymentScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repayment-schedule")
public class RepaymentScheduleController {

    @Autowired
    private RepaymentScheduleService repaymentScheduleService;

    @PostMapping("/generate/{loanId}")
    public ResponseEntity<String> generateSchedule(@PathVariable Long loanId) {
        repaymentScheduleService.generateRepaymentSchedule(loanId);
        return ResponseEntity.ok("Repayment schedule generated for loan ID: " + loanId);
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<List<RepaymentScheduleDTO>> getSchedule(@PathVariable Long loanId) {
        List<RepaymentScheduleDTO> schedule = repaymentScheduleService.getRepaymentSchedule(loanId);
        return ResponseEntity.ok(schedule);
    }
}

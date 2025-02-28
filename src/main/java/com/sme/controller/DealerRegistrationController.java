package com.sme.controller;

import com.sme.dto.DealerRegistrationDTO;

import com.sme.service.DealerRegistrationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dealers")
public class DealerRegistrationController {

    @Autowired
    private DealerRegistrationService dealerService;

    // ✅ Create Dealer
    @PostMapping
    public ResponseEntity<DealerRegistrationDTO> createDealer(@RequestBody DealerRegistrationDTO dealerDTO) {
        return ResponseEntity.ok(dealerService.createDealer(dealerDTO));
    }

    // ✅ Get All Dealers
    @GetMapping
    public ResponseEntity<List<DealerRegistrationDTO>> getAllDealers() {
        return ResponseEntity.ok(dealerService.getAllDealers());
    }

    // ✅ Get Dealer by ID
    @GetMapping("/{id}")
    public ResponseEntity<DealerRegistrationDTO> getDealerById(@PathVariable Long id) {
        return ResponseEntity.ok(dealerService.getDealerById(id));
    }

 
    @PutMapping("/{id}")
    public ResponseEntity<DealerRegistrationDTO> updateDealer(
            @PathVariable Long id,
            @RequestBody DealerRegistrationDTO dealerDTO) {
        return ResponseEntity.ok(dealerService.updateDealer(id, dealerDTO));
    }

    // ✅ Delete Dealer
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDealer(@PathVariable Long id) {
        dealerService.deleteDealer(id);
        return ResponseEntity.ok("Dealer deleted successfully.");
    }
}

package com.sme.controller;

import com.sme.dto.DealerRegistrationDTO;
import com.sme.service.DealerRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dealer-registration")
public class DealerRegistrationController {

    @Autowired
    private DealerRegistrationService dealerService;

    @GetMapping
    public List<DealerRegistrationDTO> getDealerRegistrations() {
        return dealerService.getAllDealerRegistrations();
    }

    @PostMapping
    public ResponseEntity<DealerRegistrationDTO> createDealer(@RequestBody DealerRegistrationDTO dto) {
        return ResponseEntity.ok(dealerService.createDealer(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DealerRegistrationDTO> updateDealer(@PathVariable Long id, @RequestBody DealerRegistrationDTO dto) {
        return ResponseEntity.ok(dealerService.updateDealer(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DealerRegistrationDTO> getDealer(@PathVariable("id") Long id) {
        return ResponseEntity.ok(dealerService.getDealer(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDealer(@PathVariable Long id) {
        dealerService.deleteDealer(id);
        return ResponseEntity.noContent().build();
    }
}

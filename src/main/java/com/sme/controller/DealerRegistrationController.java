package com.sme.controller;

import com.sme.dto.DealerRegistrationDTO;
import com.sme.repository.DealerRegistrationRepository;
import com.sme.service.DealerRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dealer-registration")
public class DealerRegistrationController {

    @Autowired
    private DealerRegistrationService dealerService;

    @Autowired
    private DealerRegistrationRepository dealerRegistrationRepository;

    @PreAuthorize("hasRole('DEALER_READ')")
    @GetMapping("/all")
    public List<DealerRegistrationDTO> getDealerRegistrations() {
        return dealerService.getAllDealerRegistrations();
    }

    @PreAuthorize("hasRole('DEALER_CREATE')")
    @PostMapping
    public ResponseEntity<DealerRegistrationDTO> createDealer(@RequestBody DealerRegistrationDTO dto) {
        return ResponseEntity.ok(dealerService.createDealer(dto));
    }

    @PreAuthorize("hasAuthority('DEALER_CREATE')")
    @GetMapping("/check-duplicate")
    public ResponseEntity<Map<String, Boolean>> checkDuplicate(
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String phoneNumber) {
        boolean isDuplicate = dealerRegistrationRepository.existsByCompanyName(companyName) ||
                dealerRegistrationRepository.existsByPhoneNumber(phoneNumber);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isDuplicate", isDuplicate);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('DEALER_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<DealerRegistrationDTO> updateDealer(@PathVariable Long id, @RequestBody DealerRegistrationDTO dto) {
        return ResponseEntity.ok(dealerService.updateDealer(id, dto));
    }

    @PreAuthorize("hasRole('DEALER_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<DealerRegistrationDTO> getDealer(@PathVariable("id") Long id) {
        return ResponseEntity.ok(dealerService.getDealer(id));
    }

    @PreAuthorize("hasRole('DEALER_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDealer(@PathVariable Long id) {
        dealerService.deleteDealer(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('DEALER_READ')")
    @GetMapping("/activeDealers")
    public ResponseEntity<List<DealerRegistrationDTO>> getAllActiveDealers(
            @RequestParam(required = false) Long branchId) {
        List<DealerRegistrationDTO> dealerRegistrationDTOS = dealerService.getAllActiveDealer(branchId);
        return ResponseEntity.ok(dealerRegistrationDTOS);
    }

    @PreAuthorize("hasAuthority('DEALER_READ')")
    @GetMapping("/InactiveDealers")
    public ResponseEntity<List<DealerRegistrationDTO>> getFreezeCurrentAccounts(
            @RequestParam(required = false) Long branchId) {
        List<DealerRegistrationDTO> dealerRegistrationDTOS = dealerService.getAllInActiveDealer(branchId);
        return ResponseEntity.ok(dealerRegistrationDTOS);
    }
}

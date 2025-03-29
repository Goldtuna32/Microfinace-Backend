package com.sme.controller;

import com.sme.dto.HpRegistrationDTO;
import com.sme.service.HpRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hp-registrations")
public class HpRegistrationController {

    @Autowired
    private HpRegistrationService service;

    @PreAuthorize("hasRole('HP_REGISTER_READ')")
    @GetMapping
    public List<HpRegistrationDTO> getAllHpRegistrations() {
        return service.getAllHpRegistrations();
    }

    @PreAuthorize("hasRole('HP_REGISTER_READ')")
    @GetMapping("/{id}")
    public HpRegistrationDTO getHpRegistrationById(@PathVariable Long id) {
        return service.getHpRegistrationById(id);
    }

    @PreAuthorize("hasRole('HP_REGISTER_CREATE')")
    @PostMapping
    public HpRegistrationDTO createHpRegistration(@RequestBody HpRegistrationDTO dto) {
        return service.createHpRegistration(dto);
    }



    @PreAuthorize("hasRole('HP_REGISTER_UPDATE')")
    @PutMapping("/{id}")
    public HpRegistrationDTO updateHpRegistration(@PathVariable Long id, @RequestBody HpRegistrationDTO dto) {
        return service.updateHpRegistration(id, dto);
    }

    @PreAuthorize("hasRole('HP_REGISTER_DELETE')")
    @DeleteMapping("/{id}")
    public void deleteHpRegistration(@PathVariable Long id) {
        service.deleteHpRegistration(id);
    }

    @PreAuthorize("hasAuthority('HP_REGISTER_READ')")
    @GetMapping("/pendingHP")
    public ResponseEntity<List<HpRegistrationDTO>> getAllPendingHP(
            @RequestParam(required = false) Long branchId) {
        List<HpRegistrationDTO> hpRegistrationDTOS = service.getAllPendingHP(branchId);
        return ResponseEntity.ok(hpRegistrationDTOS);
    }

//    @PutMapping("/{id}/approve")
//    public ResponseEntity<HpRegistrationDTO> approveHpRegistration(
//            @PathVariable Long id,
//            @RequestBody Map<String, BigDecimal> request) throws Exception {
//
//        BigDecimal bankPortion = request.get("bankPortion");
//        HpRegistrationDTO approvedHp = service.approveHpRegistration(id, bankPortion);
//        return ResponseEntity.ok(approvedHp);
//    }


    @PreAuthorize("hasAuthority('HP_REGISTER_READ')")
    @GetMapping("/approvedHP")
    public ResponseEntity<List<HpRegistrationDTO>> getAllApprovedHP(
            @RequestParam(required = false) Long branchId) {
        List<HpRegistrationDTO> hpRegistrationDTOS = service.getAllApprovedHP(branchId);
        return ResponseEntity.ok(hpRegistrationDTOS);
    }
}

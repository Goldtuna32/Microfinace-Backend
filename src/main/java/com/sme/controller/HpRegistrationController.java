package com.sme.controller;

import com.sme.dto.HpRegistrationDTO;
import com.sme.service.HpRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hp-registrations")
public class HpRegistrationController {

    @Autowired
    private HpRegistrationService service;

    @GetMapping
    public List<HpRegistrationDTO> getAllHpRegistrations() {
        return service.getAllHpRegistrations();
    }

    @GetMapping("/{id}")
    public HpRegistrationDTO getHpRegistrationById(@PathVariable Long id) {
        return service.getHpRegistrationById(id);
    }


    @PostMapping
    public ResponseEntity<HpRegistrationDTO> createHpRegistration(@RequestBody HpRegistrationDTO hpDto) {
        HpRegistrationDTO savedHp = service.save(hpDto);
        return ResponseEntity.ok(savedHp);
    }

    @PutMapping("/{id}")
    public HpRegistrationDTO updateHpRegistration(@PathVariable Long id, @RequestBody HpRegistrationDTO dto) {
        return service.updateHpRegistration(id, dto);
    }

    @PutMapping("/{id}/delete")
    public void softDeleteHpRegistration(@PathVariable Long id) {
        service.softDeleteHpRegistration(id);
    }

    // Restore: set status back to 1
    @PutMapping("/{id}/restore")
    public void restoreHpRegistration(@PathVariable Long id) {
        service.restoreHpRegistration(id);

    }



}

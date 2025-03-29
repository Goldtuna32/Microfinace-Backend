package com.sme.controller;

import com.sme.dto.HpRegistrationDTO;
import com.sme.service.HpRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}

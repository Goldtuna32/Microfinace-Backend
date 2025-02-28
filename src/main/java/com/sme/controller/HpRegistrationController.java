package com.sme.controller;

import com.sme.dto.HpRegistrationDTO;
import com.sme.service.HpRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public HpRegistrationDTO createHpRegistration(@RequestBody HpRegistrationDTO dto) {
        return service.createHpRegistration(dto);
    }

    @PutMapping("/{id}")
    public HpRegistrationDTO updateHpRegistration(@PathVariable Long id, @RequestBody HpRegistrationDTO dto) {
        return service.updateHpRegistration(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteHpRegistration(@PathVariable Long id) {
        service.deleteHpRegistration(id);
    }
}

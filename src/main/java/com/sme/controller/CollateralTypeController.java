package com.sme.controller;

import com.sme.dto.CollateralTypeDTO;
import com.sme.entity.CollateralType;
import com.sme.repository.CollateralTypeRepository;
import com.sme.service.CollateralTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/collateral-types")
@CrossOrigin("http://localhost:4200")
public class CollateralTypeController {

    @Autowired
    private CollateralTypeService service;

    @Autowired
    private CollateralTypeRepository collateralTypeRepository;

    @PreAuthorize("hasRole('COLLATERAL_TYPE_CREATE')")
    @PostMapping("/create")
    public ResponseEntity<CollateralTypeDTO> create(@RequestBody CollateralTypeDTO dto) {
        CollateralType entity = new CollateralType();
        entity.setName(dto.getName());
        entity.setStatus(1); // Always create as active
        CollateralType savedEntity = service.createCollateralType(entity);
        dto.setId(savedEntity.getId());
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasAuthority('COLLATERAL_TYPE_CREATE')")
    @GetMapping("/check-duplicate")
    public ResponseEntity<Map<String, Boolean>> checkDuplicate(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String email) {
        boolean isDuplicate = collateralTypeRepository.existsByName(name);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isDuplicate", isDuplicate);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('COLLATERAL_TYPE_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<CollateralTypeDTO> getById(@PathVariable Long id) {
        CollateralType entity = service.getCollateralTypeById(id);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        CollateralTypeDTO dto = new CollateralTypeDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setStatus(entity.getStatus());
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasRole('COLLATERAL_TYPE_READ')")
    @GetMapping("/active")
    public ResponseEntity<List<CollateralTypeDTO>> getAllActive() {
        List<CollateralTypeDTO> dtos = service.getAllActiveCollateralTypes().stream().map(entity -> {
            CollateralTypeDTO dto = new CollateralTypeDTO();
            dto.setId(entity.getId());
            dto.setName(entity.getName());
            dto.setStatus(entity.getStatus());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PreAuthorize("hasRole('COLLATERAL_TYPE_READ')")
    @GetMapping("/deleted")
    public ResponseEntity<List<CollateralTypeDTO>> getAllDeleted() {
        List<CollateralTypeDTO> dtos = service.getAllDeletedCollateralTypes().stream().map(entity -> {
            CollateralTypeDTO dto = new CollateralTypeDTO();
            dto.setId(entity.getId());
            dto.setName(entity.getName());
            dto.setStatus(entity.getStatus());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PreAuthorize("hasRole('COLLATERAL_TYPE_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<CollateralTypeDTO> update(@PathVariable Long id, @RequestBody CollateralTypeDTO dto) {
        CollateralType entity = new CollateralType();
        entity.setName(dto.getName());
        entity.setStatus(dto.getStatus());
        CollateralType updatedEntity = service.updateCollateralType(id, entity);
        if (updatedEntity == null) {
            return ResponseEntity.notFound().build();
        }
        dto.setId(updatedEntity.getId());
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasRole('COLLATERAL_TYPE_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        service.softDeleteCollateralType(id);
        return ResponseEntity.noContent().build();
    }
}
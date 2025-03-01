package com.sme.controller;

import com.sme.dto.CollateralTypeDTO;
import com.sme.entity.CollateralType;
import com.sme.service.CollateralTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/collateral-types")
public class CollateralTypeController {

    @Autowired
    private CollateralTypeService service;

    @PostMapping("/create")
    public ResponseEntity<CollateralTypeDTO> create(@RequestBody CollateralTypeDTO dto) {
        CollateralType entity = new CollateralType();
        entity.setName(dto.getName());
        entity.setStatus(1); // Always create as active
        CollateralType savedEntity = service.createCollateralType(entity);
        dto.setId(savedEntity.getId());
        return ResponseEntity.ok(dto);
    }

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

    @GetMapping
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        service.softDeleteCollateralType(id);
        return ResponseEntity.noContent().build();
    }
}
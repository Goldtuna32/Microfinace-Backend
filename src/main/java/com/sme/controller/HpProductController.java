package com.sme.controller;

import com.sme.dto.HpProductDTO;
import com.sme.repository.HpProductRepository;
import com.sme.service.HpProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hp-products")
public class HpProductController {

    @Autowired
    private HpProductService hpProductService;

    @Autowired
    private HpProductRepository hpProductRepository;

    @PreAuthorize("hasRole('HP_PRODUCT_READ')")
    @GetMapping
    public List<HpProductDTO> getAllHpProducts() {
        return hpProductService.getAllHpProducts();
    }

    @PreAuthorize("hasRole('HP_PRODUCT_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<HpProductDTO> getHpProductById(@PathVariable Long id) {
        HpProductDTO hpProductDTO = hpProductService.getHpProductById(id);
        if (hpProductDTO != null) {
            return ResponseEntity.ok(hpProductDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('HP_PRODUCT_CREATE')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HpProductDTO> createHpProduct(
            @RequestPart("hpProduct") HpProductDTO hpProductDTO,
            @RequestPart("photo") MultipartFile photo) {
        String photoUrl = hpProductService.uploadImage(photo);
        hpProductDTO.setHpProductPhoto(photoUrl);

        // Create the product
        HpProductDTO createdProduct = hpProductService.createHpProduct(hpProductDTO);
        return ResponseEntity.ok(createdProduct);
    }

    @PreAuthorize("hasAuthority('HP_PRODUCT_CREATE')")
    @GetMapping("/check-duplicate")
    public ResponseEntity<Map<String, Boolean>> checkDuplicate(
            @RequestParam(required = false) String name) {
        boolean isDuplicate = hpProductRepository.existsByName(name);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isDuplicate", isDuplicate);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('HP_PRODUCT_UPDATE')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HpProductDTO> updateHpProduct(
            @PathVariable Long id,
            @RequestPart("hpProduct") HpProductDTO hpProductDTO,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        // If a new photo is provided, handle it in the service
        HpProductDTO updatedHpProductDTO = hpProductService.updateHpProduct(id, hpProductDTO, photo);
        if (updatedHpProductDTO != null) {
            return ResponseEntity.ok(updatedHpProductDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('HP_PRODUCT_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHpProduct(@PathVariable Long id) {
        hpProductService.deleteHpProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('HP_PRODUCT_DELETE')")
    @PutMapping("/{id}/restore")
    public ResponseEntity<HpProductDTO> restoreHpProduct(@PathVariable Long id) {
        HpProductDTO restored = hpProductService.restoreHpProduct(id);
        return restored != null ? ResponseEntity.ok(restored) : ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasAuthority('HP_PRODUCT_READ')")
    @GetMapping("/activeHpProducts")
    public ResponseEntity<List<HpProductDTO>> getAllActiveProducts(
            @RequestParam(required = false) Long branchId) {
        List<HpProductDTO> hpProductDTOS = hpProductService.getAllActiveProducts(branchId);
        return ResponseEntity.ok(hpProductDTOS);
    }

    @PreAuthorize("hasAuthority('HP_PRODUCT_READ')")
    @GetMapping("/inActiveHpProducts")
    public ResponseEntity<List<HpProductDTO>> getAllInactiveProducts(
            @RequestParam(required = false) Long branchId) {
        List<HpProductDTO> hpProductDTOS = hpProductService.getAllInActiveProducts(branchId);
        return ResponseEntity.ok(hpProductDTOS);
    }
}

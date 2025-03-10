package com.sme.controller;

import com.sme.dto.HpProductDTO;
import com.sme.service.HpProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/hp-products")
public class HpProductController {

    @Autowired
    private HpProductService hpProductService;

    @GetMapping
    public List<HpProductDTO> getAllHpProducts() {
        return hpProductService.getAllHpProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<HpProductDTO> getHpProductById(@PathVariable Long id) {
        HpProductDTO hpProductDTO = hpProductService.getHpProductById(id);
        if (hpProductDTO != null) {
            return ResponseEntity.ok(hpProductDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHpProduct(@PathVariable Long id) {
        hpProductService.deleteHpProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<HpProductDTO> restoreHpProduct(@PathVariable Long id) {
        HpProductDTO restored = hpProductService.restoreHpProduct(id);
        return restored != null ? ResponseEntity.ok(restored) : ResponseEntity.notFound().build();
    }
}

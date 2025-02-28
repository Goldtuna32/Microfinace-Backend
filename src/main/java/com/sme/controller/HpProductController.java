package com.sme.controller;

import com.sme.dto.HpProductDTO;

import com.sme.service.HpProductService;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class HpProductController {

    @Autowired
    private HpProductService productService;
 
    // ✅ Create HP Product
    @PostMapping
    public ResponseEntity<HpProductDTO> createHpProduct(@RequestBody HpProductDTO productDTO) {
        return ResponseEntity.ok(productService.createHpProduct(productDTO));
    }

    // ✅ Get All HP Products
    @GetMapping
    public ResponseEntity<List<HpProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

}

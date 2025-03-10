package com.sme.controller;

import com.sme.dto.ProductTypeDTO;
import com.sme.service.ProductTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-types")
public class ProductTypeController {

    @Autowired
    private ProductTypeService productTypeService;

    @GetMapping
    public List<ProductTypeDTO> getAllProductTypes() {
        return productTypeService.getAllProductTypes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductTypeDTO> getProductTypeById(@PathVariable Long id) {
        ProductTypeDTO productTypeDTO = productTypeService.getProductTypeById(id);
        if (productTypeDTO != null) {
            return ResponseEntity.ok(productTypeDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ProductTypeDTO createProductType(@RequestBody ProductTypeDTO productTypeDTO) {
        return productTypeService.createProductType(productTypeDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductTypeDTO> updateProductType(@PathVariable Long id, @RequestBody ProductTypeDTO productTypeDTO) {
        ProductTypeDTO updatedProductTypeDTO = productTypeService.updateProductType(id, productTypeDTO);
        if (updatedProductTypeDTO != null) {
            return ResponseEntity.ok(updatedProductTypeDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductType(@PathVariable Long id) {
        productTypeService.deleteProductType(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<ProductTypeDTO> restoreProductType(@PathVariable Long id) {
        ProductTypeDTO restored = productTypeService.restoreProductType(id);
        return ResponseEntity.ok(restored);
    }
}

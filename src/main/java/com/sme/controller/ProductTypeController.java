package com.sme.controller;

import com.sme.dto.ProductTypeDTO;
import com.sme.repository.ProductTypeRepository;
import com.sme.service.ProductTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product-types")
public class ProductTypeController {

    @Autowired
    private ProductTypeService productTypeService;

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @PreAuthorize("hasRole('PRODUCT_TYPE_READ')")
    @GetMapping
    public List<ProductTypeDTO> getAllProductTypes() {
        return productTypeService.getAllProductTypes();
    }

    @PreAuthorize("hasRole('PRODUCT_TYPE_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<ProductTypeDTO> getProductTypeById(@PathVariable Long id) {
        ProductTypeDTO productTypeDTO = productTypeService.getProductTypeById(id);
        if (productTypeDTO != null) {
            return ResponseEntity.ok(productTypeDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('PRODUCT_TYPE_CREATE')")
    @PostMapping
    public ProductTypeDTO createProductType(@RequestBody ProductTypeDTO productTypeDTO) {
        return productTypeService.createProductType(productTypeDTO);
    }

    @PreAuthorize("hasAuthority('PRODUCT_TYPE_CREATE')")
    @GetMapping("/check-duplicate")
    public ResponseEntity<Map<String, Boolean>> checkDuplicate(
            @RequestParam(required = false) String name) {
        boolean isDuplicate = productTypeRepository.existsByName(name);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isDuplicate", isDuplicate);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PRODUCT_TYPE_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductTypeDTO> updateProductType(@PathVariable Long id, @RequestBody ProductTypeDTO productTypeDTO) {
        ProductTypeDTO updatedProductTypeDTO = productTypeService.updateProductType(id, productTypeDTO);
        if (updatedProductTypeDTO != null) {
            return ResponseEntity.ok(updatedProductTypeDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('PRODUCT_TYPE_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductType(@PathVariable Long id) {
        productTypeService.deleteProductType(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('PRODUCT_TYPE_READ')")
    @PutMapping("/{id}/restore")
    public ResponseEntity<ProductTypeDTO> restoreProductType(@PathVariable Long id) {
        ProductTypeDTO restored = productTypeService.restoreProductType(id);
        return ResponseEntity.ok(restored);
    }
}

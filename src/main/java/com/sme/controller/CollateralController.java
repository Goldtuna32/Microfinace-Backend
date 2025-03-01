package com.sme.controller;

import com.sme.dto.CollateralDTO;
import com.sme.service.CollateralService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

@RestController
@RequestMapping("/api/collaterals")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST,
        RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS }, allowCredentials = "true")
public class CollateralController {

    private final CollateralService collateralService;

    @GetMapping
    public ResponseEntity<List<CollateralDTO>> getAllCollaterals() {
        return ResponseEntity.ok(collateralService.getAllCollaterals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollateralDTO> getCollateralById(@PathVariable Long id) {
        return collateralService.getCollateralById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<CollateralDTO> createCollateral(
            @ModelAttribute CollateralDTO collateralDTO,
            @RequestParam(value = "F_collateralPhoto", required = false) MultipartFile frontPhoto,
            @RequestParam(value = "B_collateralPhoto", required = false) MultipartFile backPhoto) throws IOException {
        return ResponseEntity.ok(collateralService.createCollateral(collateralDTO, frontPhoto, backPhoto));
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<CollateralDTO> updateCollateral(
            @PathVariable Long id,
            @RequestParam("value") String value,
            @RequestParam("description") String description,
            @RequestParam("status") Integer status,
            @RequestParam(value = "f_collateral_photo", required = false) MultipartFile frontPhoto,
            @RequestParam(value = "b_collateral_photo", required = false) MultipartFile backPhoto
    ) throws IOException {



        // Build CollateralDTO
        CollateralDTO collateralDTO = new CollateralDTO();
        collateralDTO.setId(id);
        collateralDTO.setValue(new BigDecimal(value)); // Parse string to BigDecimal
        collateralDTO.setDescription(description);
        collateralDTO.setStatus(status);


        // Call service and return response
        CollateralDTO updatedCollateral = collateralService.updateCollateral(id, collateralDTO, frontPhoto, backPhoto);
        return ResponseEntity.ok(updatedCollateral);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollateral(@PathVariable Long id) {
        return collateralService.deleteCollateral(id) ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<CollateralDTO>> getAllCollateralsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(collateralService.getAllCollateralsPaginated(pageable));
    }
}

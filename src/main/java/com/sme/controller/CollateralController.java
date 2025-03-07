package com.sme.controller;

import com.sme.dto.CollateralDTO;
import com.sme.dto.SmeLoanCollateralDTO;
import com.sme.entity.SmeLoanCollateral;
import com.sme.repository.SmeLoanCollateralRepository;
import com.sme.service.CollateralService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

@RestController
@RequestMapping("/api/collaterals")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST,
        RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS }, allowCredentials = "true")
public class CollateralController {

    @Autowired
    private SmeLoanCollateralRepository smeLoanCollateralRepository;

    private final CollateralService collateralService;

    @GetMapping("/active")
    public ResponseEntity<Page<CollateralDTO>> getAllCollaterals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        List<CollateralDTO> allCollaterals = collateralService.getAllCollaterals();

        Comparator<CollateralDTO> comparator;
        switch (sortBy.toLowerCase()) {
            case "name":
                comparator = Comparator.comparing(CollateralDTO::getDescription);
                break;
            default:
                comparator = Comparator.comparing(CollateralDTO::getId);
        }
        if (direction.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }
        List<CollateralDTO> sortedCollaterals = allCollaterals.stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page, size);
        int start = Math.min((int) pageable.getOffset(), sortedCollaterals.size());
        int end = Math.min(start + pageable.getPageSize(), sortedCollaterals.size());
        List<CollateralDTO> paginatedCollaterals = sortedCollaterals.subList(start, end);

        Page<CollateralDTO> pageResult = new PageImpl<>(paginatedCollaterals, pageable, sortedCollaterals.size());
        return ResponseEntity.ok(pageResult);
    }

    @GetMapping("/deleted")
    public ResponseEntity<Page<CollateralDTO>> getDeletedCollaterals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        List<CollateralDTO> allCollaterals = collateralService.getDeletedCollaterals();


        Comparator<CollateralDTO> comparator;
        switch (sortBy.toLowerCase()) {
            case "name":
                comparator = Comparator.comparing(CollateralDTO::getDescription);
                break;
            default:
                comparator = Comparator.comparing(CollateralDTO::getId);
        }
        if (direction.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }
        List<CollateralDTO> sortedCollaterals = allCollaterals.stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page, size);
        int start = Math.min((int) pageable.getOffset(), sortedCollaterals.size());
        int end = Math.min(start + pageable.getPageSize(), sortedCollaterals.size());
        List<CollateralDTO> paginatedCollaterals = sortedCollaterals.subList(start, end);

        Page<CollateralDTO> pageResult = new PageImpl<>(paginatedCollaterals, pageable, sortedCollaterals.size());
        return ResponseEntity.ok(pageResult);
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
    public ResponseEntity<Void> softDeleteCollateral(@PathVariable Long id) {
        return collateralService.softDeleteCollateral(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<Void> restoreCollateral(@PathVariable Long id) {
        return collateralService.restoreCollateral(id)
                ? ResponseEntity.noContent().build()
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

    @GetMapping("/cif/{cifId}")
    public ResponseEntity<List<CollateralDTO>> getCollateralsByCifId(@PathVariable Long cifId) {
        List<CollateralDTO> collaterals = collateralService.getCollateralsByCifId(cifId);
        return ResponseEntity.ok(collaterals);
    }

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<SmeLoanCollateralDTO>> getCollateralsByLoanId(@PathVariable Long loanId) {
        List<SmeLoanCollateral> collaterals = smeLoanCollateralRepository.findBySmeLoanId(loanId);
        List<SmeLoanCollateralDTO> dtos = collaterals.stream()
                .map(coll -> {
                    SmeLoanCollateralDTO dto = new SmeLoanCollateralDTO();
                    dto.setCollateralId(coll.getCollateral().getId());
                    dto.setCollateralAmount(coll.getCollateralAmount());
                    dto.setDescription(coll.getCollateral().getDescription());
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}

package com.sme.controller;

import com.sme.dto.CIFDTO;
import com.sme.service.CIFService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cifs")
@CrossOrigin("http://localhost:4200")
@RequiredArgsConstructor
public class CIFController {

    @Autowired
    private CIFService cifService;

    @GetMapping("/active")
    public ResponseEntity<Page<CIFDTO>> getAllCIFs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        // Fetch all active CIFs
        List<CIFDTO> allCifs = cifService.getAllCIFs();

        // Apply sorting
        Comparator<CIFDTO> comparator;
        switch (sortBy.toLowerCase()) {
            case "name":
                comparator = Comparator.comparing(CIFDTO::getName);
                break;
            case "nrcnumber":
                comparator = Comparator.comparing(CIFDTO::getNrcNumber);
                break;
            // Add other sortable fields as needed
            default:
                comparator = Comparator.comparing(CIFDTO::getId);
        }
        if (direction.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }
        List<CIFDTO> sortedCifs = allCifs.stream().sorted(comparator).collect(Collectors.toList());

        // Apply pagination
        Pageable pageable = PageRequest.of(page, size);
        int start = Math.min((int) pageable.getOffset(), sortedCifs.size());
        int end = Math.min(start + pageable.getPageSize(), sortedCifs.size());
        List<CIFDTO> paginatedCifs = sortedCifs.subList(start, end);

        Page<CIFDTO> pageResult = new PageImpl<>(paginatedCifs, pageable, sortedCifs.size());
        return ResponseEntity.ok(pageResult);
    }

    @GetMapping("/deleted")
    public ResponseEntity<Page<CIFDTO>> getDeletedCIFs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        // Fetch all deleted CIFs
        List<CIFDTO> allCifs = cifService.getDeletedCIFs();

        // Apply sorting
        Comparator<CIFDTO> comparator;
        switch (sortBy.toLowerCase()) {
            case "name":
                comparator = Comparator.comparing(CIFDTO::getName);
                break;
            case "nrcnumber":
                comparator = Comparator.comparing(CIFDTO::getNrcNumber);
                break;
            // Add other sortable fields as needed
            default:
                comparator = Comparator.comparing(CIFDTO::getId);
        }
        if (direction.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }
        List<CIFDTO> sortedCifs = allCifs.stream().sorted(comparator).collect(Collectors.toList());

        // Apply pagination
        Pageable pageable = PageRequest.of(page, size);
        int start = Math.min((int) pageable.getOffset(), sortedCifs.size());
        int end = Math.min(start + pageable.getPageSize(), sortedCifs.size());
        List<CIFDTO> paginatedCifs = sortedCifs.subList(start, end);

        Page<CIFDTO> pageResult = new PageImpl<>(paginatedCifs, pageable, sortedCifs.size());
        return ResponseEntity.ok(pageResult);
    }


    @GetMapping("/cif/{id}")
    public ResponseEntity<?> getCIFById(@PathVariable Long id) {
        Optional<CIFDTO> cifDTO = cifService.getCIFById(id);

        if (cifDTO.isPresent()) {
            return ResponseEntity.ok(cifDTO.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("CIF not found");
        }
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CIFDTO> createCIF(
            @RequestParam("name") String name,
            @RequestParam("nrcNumber") String nrcNumber,
            @RequestParam("dob") String dob,
            @RequestParam("gender") String gender,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("email") String email,
            @RequestParam("address") String address,
            @RequestParam("maritalStatus") String maritalStatus,
            @RequestParam("occupation") String occupation,
            @RequestParam("incomeSource") String incomeSource,
            @RequestParam("branchId") Long branchId,
            @RequestParam(value = "fNrcPhotoUrl", required = false) MultipartFile frontNrc,
            @RequestParam(value = "bNrcPhotoUrl", required = false) MultipartFile backNrc
    ) throws IOException {
        CIFDTO cifDTO = CIFDTO.builder()
                .name(name)
                .nrcNumber(nrcNumber)
                .dob(LocalDate.parse(dob))
                .gender(gender)
                .phoneNumber(phoneNumber)
                .email(email)
                .address(address)
                .maritalStatus(maritalStatus)
                .occupation(occupation)
                .incomeSource(incomeSource)
                .branchId(branchId)
                .build();

        return ResponseEntity.ok(cifService.createCIF(cifDTO, frontNrc, backNrc));
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<CIFDTO> updateCIF(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("nrcNumber") String nrcNumber,
            @RequestParam("dob") String dob,
            @RequestParam("gender") String gender,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("email") String email,
            @RequestParam("address") String address,
            @RequestParam("maritalStatus") String maritalStatus,
            @RequestParam("occupation") String occupation,
            @RequestParam("incomeSource") String incomeSource,
            @RequestParam("branchId") Long branchId,
            @RequestParam(value = "fNrcPhotoUrl", required = false) MultipartFile frontNrc,
            @RequestParam(value = "bNrcPhotoUrl", required = false) MultipartFile backNrc
    ) throws IOException {

        CIFDTO cifDTO = CIFDTO.builder()
                .name(name)
                .nrcNumber(nrcNumber)
                .dob(LocalDate.parse(dob))
                .gender(gender)
                .phoneNumber(phoneNumber)
                .email(email)
                .address(address)
                .maritalStatus(maritalStatus)
                .occupation(occupation)
                .incomeSource(incomeSource)
                .branchId(branchId)
                .build();

        return ResponseEntity.ok(cifService.updateCIF(id, cifDTO, frontNrc, backNrc));
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteCIF(@PathVariable Long id) {
        return cifService.softDeleteCIF(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<Void> restoreCIF(@PathVariable Long id) {
        return cifService.restoreCIF(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}

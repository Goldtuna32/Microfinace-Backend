package com.sme.controller;

import com.sme.dto.CIFDTO;
import com.sme.repository.CIFRepository;
import com.sme.service.CIFService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cifs")
@CrossOrigin("http://localhost:4200")
@RequiredArgsConstructor
public class CIFController {

    @Autowired
    private CIFService cifService;

    @Autowired
    private CIFRepository cifRepository;

    @GetMapping("/active")
    public ResponseEntity<Page<CIFDTO>> getAllCIFs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String nrcPrefix) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CIFDTO> cifPage = cifService.getAllCIFs(pageable, nrcPrefix);
        return ResponseEntity.ok(cifPage);
    }

    @GetMapping("/check-duplicate")
    public ResponseEntity<Map<String, Boolean>> checkDuplicate(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String nrcNumber,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String email) {
        boolean isDuplicate = cifRepository.existsByName(name) ||
                cifRepository.existsByNrcNumber(nrcNumber) ||
                cifRepository.existsByPhoneNumber(phoneNumber) ||
                cifRepository.existsByEmail(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isDuplicate", isDuplicate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/deleted")
    public ResponseEntity<Page<CIFDTO>> getDeletedCIFs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String nrcPrefix) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CIFDTO> cifPage = cifService.getDeletedCIFs(pageable, nrcPrefix);
        return ResponseEntity.ok(cifPage);
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

package com.sme.controller;

import com.sme.dto.CIFDTO;
import com.sme.service.CIFService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cifs")
@CrossOrigin("http://localhost:4200")
@RequiredArgsConstructor
public class CIFController {

    @Autowired
    private CIFService cifService;

    @GetMapping
    public ResponseEntity<List<CIFDTO>> getAllCIFs() {
        List<CIFDTO> cifList = cifService.getAllCIFs();
        return ResponseEntity.ok(cifList);
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



    // ✅ Delete CIF
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCIF(@PathVariable Long id) {
        cifService.deleteCIF(id);
        return ResponseEntity.noContent().build();
    }
}

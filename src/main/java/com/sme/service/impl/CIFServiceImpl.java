package com.sme.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.sme.dto.CIFDTO;
import com.sme.entity.Branch;
import com.sme.entity.CIF;
import com.sme.repository.BranchRepository;
import com.sme.repository.CIFRepository;
import com.sme.service.CIFService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CIFServiceImpl implements CIFService {

    private final CIFRepository cifRepository;
    private final BranchRepository branchRepository;
    private final ModelMapper modelMapper;

    @Autowired
    private CurrentAccountServiceImpl currentAccountService;

    private final Cloudinary cloudinary;

    @Override
    public List<CIFDTO> getAllCIFs() {
        return cifRepository.findByStatus(1).stream()
                .map(cif -> modelMapper.map(cif, CIFDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CIFDTO> getCIFById(Long id) {
        return cifRepository.findById(id).map(cif -> {
            boolean hasCurrentAccount = currentAccountService.hasCurrentAccount(cif.getId());
            CIFDTO cifDTO = modelMapper.map(cif, CIFDTO.class);
            cifDTO.setHasCurrentAccount(hasCurrentAccount);
            cifDTO.setFNrcPhotoUrl(cif.getFNrcPhotoUrl());
            cifDTO.setBNrcPhotoUrl(cif.getBNrcPhotoUrl());
            return cifDTO;
        });
    }

    @Override
    @Transactional
    public CIFDTO createCIF(CIFDTO cifDTO, MultipartFile frontNrc, MultipartFile backNrc) throws IOException {
        CIF cif = modelMapper.map(cifDTO, CIF.class);
        cif.setCreatedAt(LocalDateTime.now());

        if (frontNrc != null && !frontNrc.isEmpty()) {
            String frontNrcUrl = uploadImage(frontNrc);
            cif.setFNrcPhotoUrl(frontNrcUrl);
        }

        if (backNrc != null && !backNrc.isEmpty()) {
            String backNrcUrl = uploadImage(backNrc);
            cif.setBNrcPhotoUrl(backNrcUrl);
        }

        Branch branch = branchRepository.findById(cifDTO.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found with ID: " + cifDTO.getBranchId()));
        cif.setBranch(branch);
        cif.setStatus(1);

        String serialNumber = generateSerialNumber(branch);
        cif.setSerialNumber(serialNumber);

        CIF savedCIF = cifRepository.save(cif);
        return modelMapper.map(savedCIF, CIFDTO.class);
    }

    // New method to generate the serial number
    private String generateSerialNumber(Branch branch) {
        String branchCode =  branch.getBranchCode();

        String uuidPart = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 6);

        // Combine into final serial number
        return "CIF-" + branchCode + "-" + uuidPart;
    }



    private String uploadImage(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return uploadResult.get("secure_url").toString();
    }


    @Override
    @Transactional
    public CIFDTO updateCIF(Long id, CIFDTO cifDTO, MultipartFile frontNrc, MultipartFile backNrc) throws IOException {
        CIF cif = cifRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CIF not found with ID: " + id));

        Long existingId = cif.getId();
        String existingFNrcPhotoUrl = cif.getFNrcPhotoUrl();
        String existingBNrcPhotoUrl = cif.getBNrcPhotoUrl();

        modelMapper.typeMap(CIFDTO.class, CIF.class).addMappings(mapper -> {
            mapper.skip(CIF::setSerialNumber);
        });
        modelMapper.map(cifDTO, cif);

        cif.setId(existingId);
        cif.setCreatedAt(LocalDateTime.now());
        cif.setStatus(1);

        if (frontNrc != null && !frontNrc.isEmpty()) {
            deleteImage(cif.getFNrcPhotoUrl());
            String frontNrcUrl = uploadImage(frontNrc);
            cif.setFNrcPhotoUrl(frontNrcUrl);
        } else if (cifDTO.getFNrcPhotoUrl() != null) {
            cif.setFNrcPhotoUrl(cifDTO.getFNrcPhotoUrl());
        } else {
            cif.setFNrcPhotoUrl(existingFNrcPhotoUrl);
        }

        if (backNrc != null && !backNrc.isEmpty()) {
            deleteImage(cif.getBNrcPhotoUrl());
            String backNrcUrl = uploadImage(backNrc);
            cif.setBNrcPhotoUrl(backNrcUrl);
        } else if (cifDTO.getBNrcPhotoUrl() != null) {
            cif.setBNrcPhotoUrl(cifDTO.getBNrcPhotoUrl());
        } else {
            cif.setBNrcPhotoUrl(existingBNrcPhotoUrl);
        }

        CIF updatedCIF = cifRepository.save(cif);
        return modelMapper.map(updatedCIF, CIFDTO.class);
    }

    private void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return; // No old image, nothing to delete
        }

        try {
            // Extract Public ID from Cloudinary URL
            String publicId = imageUrl.substring(imageUrl.lastIndexOf("/") + 1, imageUrl.lastIndexOf("."));

            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());  // Delete from Cloudinary
        } catch (Exception e) {
            System.err.println("Failed to delete image: " + e.getMessage());
        }
    }


    @Override
    public void deleteCIF(Long id) {
        Optional<CIF> cifOptional = cifRepository.findById(id);
        if (cifOptional.isEmpty()) {
            throw new RuntimeException("CIF not found with ID: " + id);
        }

        CIF cif = cifOptional.get();
        cif.setStatus(2);
        cifRepository.save(cif);
    }

}

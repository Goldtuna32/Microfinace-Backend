package com.sme.service.impl;

// Add this import
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException; // Add this import
import java.util.Date; // Add this import

import com.sme.dto.CollateralDTO;
import com.sme.entity.CIF;
import com.sme.entity.Collateral;
import com.sme.repository.CollateralRepository;
import com.sme.service.CollateralService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cloudinary.Cloudinary;
import com.sme.repository.CollateralTypeRepository; // Add this at class level with other repositories

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.sme.repository.CIFRepository;
import com.sme.entity.CollateralType;
import com.sme.repository.CollateralTypeRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class CollateralServiceImpl implements CollateralService {
    private final CollateralRepository collateralRepository;
    private final CIFRepository cifRepository;
    private final CollateralTypeRepository collateralTypeRepository; // Add this
    private final ModelMapper modelMapper;
    private final Cloudinary cloudinary;



    @Override
    public List<CollateralDTO> getAllCollaterals() {
        return collateralRepository.findAll().stream()
                .map(collateral -> modelMapper.map(collateral, CollateralDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CollateralDTO> getCollateralById(Long id) {
        return collateralRepository.findById(id)
                .map(collateral -> modelMapper.map(collateral, CollateralDTO.class));
    }

    private String generateCollateralCode() {
        String prefix = "COL";
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String lastCollateralCode = collateralRepository.findTopByOrderByIdDesc()
                .map(Collateral::getCollateralCode)
                .orElse(null);

        if (lastCollateralCode == null) {
            return prefix + "-" + uuid + "-0001";
        }

        try {
            String[] parts = lastCollateralCode.split("-");
            int lastNumber = Integer.parseInt(parts[2]);
            return prefix + "-" + uuid + "-" + String.format("%04d", lastNumber + 1);
        } catch (Exception e) {
            return prefix + "-" + uuid + "-0001";
        }
    }

    @Transactional
    @Override
    public CollateralDTO createCollateral(CollateralDTO collateralDTO, MultipartFile frontPhoto,
            MultipartFile backPhoto) throws IOException {
        try {
            // Add debug logging
            System.out.println("Received DTO: " + collateralDTO);
            System.out.println("CollateralTypeId: " + collateralDTO.getCollateralTypeId());

            if (collateralDTO.getId() != null && collateralRepository.existsById(collateralDTO.getId())) {
                throw new RuntimeException("Collateral with ID " + collateralDTO.getId() + " already exists!");
            }

            // First, fetch the required entities
            CIF cif = cifRepository.findById(collateralDTO.getCifId())
                    .orElseThrow(() -> new RuntimeException("CIF not found with ID: " + collateralDTO.getCifId()));

            // Changed variable name to avoid conflict
            // Updated method name to match DTO field
            CollateralType type = collateralTypeRepository.findById(collateralDTO.getCollateralTypeId())
                    .orElseThrow(() -> new RuntimeException(
                            "CollateralType not found with ID: " + collateralDTO.getCollateralTypeId()));

            // Create and set up the collateral
            Collateral collateral = new Collateral();
            collateral.setValue(collateralDTO.getValue());
            collateral.setDescription(collateralDTO.getDescription());
            collateral.setStatus(collateralDTO.getStatus());
            collateral.setDate(new Date());
            collateral.setCollateralCode(generateCollateralCode());
            collateral.setCif(cif);
            collateral.setCollateralType(type);  // Using the renamed variable

            // Handle photos
            if (frontPhoto != null && !frontPhoto.isEmpty()) {
                String frontPhotoUrl = uploadImage(frontPhoto);
                collateral.setF_collateral_photo(frontPhotoUrl);
            }

            if (backPhoto != null && !backPhoto.isEmpty()) {
                String backPhotoUrl = uploadImage(backPhoto);
                collateral.setB_collateral_photo(backPhotoUrl);
            }

            // Add debug logging before save
            System.out.println("Saving collateral with type: " + collateral.getCollateralType().getId());
            collateral = collateralRepository.save(collateral);
            return modelMapper.map(collateral, CollateralDTO.class);

        } catch (Exception e) {
            e.printStackTrace(); // Add this for better error tracking
            throw new RuntimeException("Error saving collateral: " + e.getMessage(), e);
        }
    }

    private String uploadImage(MultipartFile file) throws IOException {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Error uploading image to Cloudinary: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public Optional<CollateralDTO> updateCollateral(Long id, CollateralDTO collateralDTO) {
        return collateralRepository.findById(id).map(existingCollateral -> {
            modelMapper.map(collateralDTO, existingCollateral);
            existingCollateral.setId(id); // ✅ Ensure ID remains the same
            existingCollateral = collateralRepository.save(existingCollateral);
            return modelMapper.map(existingCollateral, CollateralDTO.class);
        });
    }

    @Transactional
    @Override
    public boolean deleteCollateral(Long id) {
        if (collateralRepository.existsById(id)) {
            collateralRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public Page<CollateralDTO> getAllCollateralsPaginated(
            Pageable pageable) {
        return collateralRepository.findAll(pageable)
                .map(collateral -> modelMapper.map(collateral, CollateralDTO.class));
    }

}

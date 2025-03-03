package com.sme.service.impl;

// Add this import
import java.time.LocalDateTime;
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
        return collateralRepository.findByStatus(1).stream()
                .map(collateral -> modelMapper.map(collateral, CollateralDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CollateralDTO> getDeletedCollaterals() {
        return collateralRepository.findByStatus(2).stream()
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

        String lastCollateralCode = collateralRepository.findTopByOrderByIdDesc()
                .map(Collateral::getCollateralCode)
                .orElse(null);

        if (lastCollateralCode == null) {
            return prefix + "-" + "-0001";
        }

        try {
            String[] parts = lastCollateralCode.split("-");
            int lastNumber = Integer.parseInt(parts[2]);
            return prefix +  "-" + String.format("%04d", lastNumber + 1);
        } catch (Exception e) {
            return prefix + "-"  + "-0001";
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

    @Override
    @Transactional
    public CollateralDTO updateCollateral(Long id, CollateralDTO collateralDTO, MultipartFile frontPhoto, MultipartFile backPhoto) throws IOException {
        // Fetch the existing entity
        Collateral existingCollateral = collateralRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collateral not found with ID: " + id));

        System.out.println("Initial CIF ID: " + (existingCollateral.getCif() != null ? existingCollateral.getCif().getId() : "null"));

        String existingFCollateralPhoto = existingCollateral.getF_collateral_photo();
        String existingBCollateralPhoto = existingCollateral.getB_collateral_photo();

        // Preserve original relationships
        CIF originalCif = existingCollateral.getCif();
        System.out.println("Original CIF captured: " + (originalCif != null ? originalCif.getId() : "null"));

        CollateralType originalCollateralType = existingCollateral.getCollateralType();

        // Manually update fields from DTO (only the ones we want to change)
        if (collateralDTO.getValue() != null) {
            existingCollateral.setValue(collateralDTO.getValue());
        }
        if (collateralDTO.getDescription() != null) {
            existingCollateral.setDescription(collateralDTO.getDescription());
        }
        existingCollateral.setStatus(1); // Hardcoded as per your logic

        // Handle front photo
        if (frontPhoto != null && !frontPhoto.isEmpty()) {
            deleteImage(existingCollateral.getF_collateral_photo());
            String frontPhotoUrl = uploadImage(frontPhoto);
            existingCollateral.setF_collateral_photo(frontPhotoUrl);
        } else if (collateralDTO.getF_collateral_photo() != null && !collateralDTO.getF_collateral_photo().isEmpty()) {
            existingCollateral.setF_collateral_photo(collateralDTO.getF_collateral_photo());
        } else {
            existingCollateral.setF_collateral_photo(existingFCollateralPhoto);
        }

        // Handle back photo
        if (backPhoto != null && !backPhoto.isEmpty()) {
            deleteImage(existingCollateral.getB_collateral_photo());
            String backPhotoUrl = uploadImage(backPhoto);
            existingCollateral.setB_collateral_photo(backPhotoUrl);
        } else if (collateralDTO.getB_collateral_photo() != null && !collateralDTO.getB_collateral_photo().isEmpty()) {
            existingCollateral.setB_collateral_photo(collateralDTO.getB_collateral_photo());
        } else {
            existingCollateral.setB_collateral_photo(existingBCollateralPhoto);
        }

        // Ensure relationships are intact (optional, but for safety)
        existingCollateral.setCif(originalCif);
        existingCollateral.setCollateralType(originalCollateralType);

        System.out.println("Before save CIF ID: " + (existingCollateral.getCif() != null ? existingCollateral.getCif().getId() : "null"));

        // Save the updated entity
        Collateral updatedCollateral = collateralRepository.save(existingCollateral);
        System.out.println("Saved CIF ID: " + (updatedCollateral.getCif() != null ? updatedCollateral.getCif().getId() : "null"));

        // Map to DTO using ModelMapper (this direction should be safe)
        CollateralDTO result = modelMapper.map(updatedCollateral, CollateralDTO.class);
        return result;
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

    @Transactional
    @Override
    public boolean softDeleteCollateral(Long id) {
        if (collateralRepository.existsById(id)) {
            Collateral collateral = collateralRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Collateral not found with ID: " + id));
            collateral.setStatus(2); // Set status to 2 (inactive)
            collateralRepository.save(collateral);
            return true;
        }
        return false;
    }

    @Transactional
    @Override
    public boolean restoreCollateral(Long id) {
        if (collateralRepository.existsById(id)) {
            Collateral collateral = collateralRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Collateral not found with ID: " + id));
            collateral.setStatus(1); // Set status to 1 (active)
            collateralRepository.save(collateral);
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

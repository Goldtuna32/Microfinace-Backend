package com.sme.service.impl;

// Add this import
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

import com.sme.dto.CurrentAccountDTO;
import com.sme.entity.CurrentAccount;
import com.sme.exception.*;
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
            return prefix + "-0001";
        }

        try {
            String[] parts = lastCollateralCode.split("-");
            if (parts.length != 2) {
                throw new InvalidCollateralCodeException(lastCollateralCode);
            }
            int lastNumber = Integer.parseInt(parts[1]);
            return prefix + "-" + String.format("%04d", lastNumber + 1);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new InvalidCollateralCodeException(
                    "Error parsing collateral code: " + lastCollateralCode, e);
        }
    }

    @Transactional
    @Override
    public CollateralDTO createCollateral(CollateralDTO collateralDTO, MultipartFile frontPhoto,
                                          MultipartFile backPhoto) throws IOException {
        // Validation
        validateCollateralDTO(collateralDTO);

        if (collateralDTO.getId() != null && collateralRepository.existsById(collateralDTO.getId())) {
            throw new DuplicateCollateralException("ID: " + collateralDTO.getId());
        }

        try {
            CIF cif = cifRepository.findById(collateralDTO.getCifId())
                    .orElseThrow(() -> new CIFNotFoundException(collateralDTO.getCifId()));

            CollateralType type = collateralTypeRepository.findById(collateralDTO.getCollateralTypeId())
                    .orElseThrow(() -> new RuntimeException(
                            "CollateralType not found with ID: " + collateralDTO.getCollateralTypeId()));

            Collateral collateral = new Collateral();
            collateral.setValue(collateralDTO.getValue());
            collateral.setDescription(collateralDTO.getDescription());
            collateral.setStatus(1); // Default active status
            collateral.setDate(new Date());
            collateral.setCollateralCode(generateCollateralCode());
            collateral.setCif(cif);
            collateral.setCollateralType(type);

            if (frontPhoto != null && !frontPhoto.isEmpty()) {
                try {
                    String frontPhotoUrl = uploadImage(frontPhoto);
                    collateral.setF_collateral_photo(frontPhotoUrl);
                } catch (IOException e) {
                    throw new ImageUploadException("Failed to upload front collateral photo", e);
                }
            }

            if (backPhoto != null && !backPhoto.isEmpty()) {
                try {
                    String backPhotoUrl = uploadImage(backPhoto);
                    collateral.setB_collateral_photo(backPhotoUrl);
                } catch (IOException e) {
                    throw new ImageUploadException("Failed to upload back collateral photo", e);
                }
            }

            Collateral savedCollateral = collateralRepository.save(collateral);
            return modelMapper.map(savedCollateral, CollateralDTO.class);
        } catch (Exception e) {
            throw new CollateralCreationException(
                    "Failed to create collateral for CIF ID: " + collateralDTO.getCifId(), e);
        }
    }

    private void validateCollateralDTO(CollateralDTO collateralDTO) {
        // Required field validation
        if (collateralDTO.getCifId() == null) {
            throw new MissingRequiredFieldException("cifId");
        }
        if (collateralDTO.getCollateralTypeId() == null) {
            throw new MissingRequiredFieldException("collateralTypeId");
        }
        if (collateralDTO.getValue() == null) {
            throw new MissingRequiredFieldException("value");
        }

        // Value validation
        if (collateralDTO.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidCollateralValueException(collateralDTO.getValue());
        }

        // Optional: Description length check (example)
        if (collateralDTO.getDescription() != null && collateralDTO.getDescription().length() > 500) {
            throw new CollateralValidationException("Description exceeds maximum length of 500 characters");
        }
    }


    private String uploadImage(MultipartFile file) throws IOException {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return uploadResult.get("secure_url").toString();
        } catch (Exception e) {
            throw new ImageUploadException("Failed to upload image to Cloudinary", e);
        }
    }

    @Override
    @Transactional
    public CollateralDTO updateCollateral(Long id, CollateralDTO collateralDTO, MultipartFile frontPhoto, MultipartFile backPhoto) throws IOException {
        Collateral existingCollateral = collateralRepository.findById(id)
                .orElseThrow(() -> new CollateralNotFoundException(id));

        // Validation
        validateCollateralDTO(collateralDTO);

        try {
            String existingFCollateralPhoto = existingCollateral.getF_collateral_photo();
            String existingBCollateralPhoto = existingCollateral.getB_collateral_photo();
            CIF originalCif = existingCollateral.getCif();
            CollateralType originalCollateralType = existingCollateral.getCollateralType();

            if (collateralDTO.getValue() != null) {
                existingCollateral.setValue(collateralDTO.getValue());
            }
            if (collateralDTO.getDescription() != null) {
                existingCollateral.setDescription(collateralDTO.getDescription());
            }
            existingCollateral.setStatus(1);

            if (frontPhoto != null && !frontPhoto.isEmpty()) {
                deleteImage(existingCollateral.getF_collateral_photo());
                String frontPhotoUrl = uploadImage(frontPhoto);
                existingCollateral.setF_collateral_photo(frontPhotoUrl);
            } else if (collateralDTO.getF_collateral_photo() != null && !collateralDTO.getF_collateral_photo().isEmpty()) {
                existingCollateral.setF_collateral_photo(collateralDTO.getF_collateral_photo());
            } else {
                existingCollateral.setF_collateral_photo(existingFCollateralPhoto);
            }

            if (backPhoto != null && !backPhoto.isEmpty()) {
                deleteImage(existingCollateral.getB_collateral_photo());
                String backPhotoUrl = uploadImage(backPhoto);
                existingCollateral.setB_collateral_photo(backPhotoUrl);
            } else if (collateralDTO.getB_collateral_photo() != null && !collateralDTO.getB_collateral_photo().isEmpty()) {
                existingCollateral.setB_collateral_photo(collateralDTO.getB_collateral_photo());
            } else {
                existingCollateral.setB_collateral_photo(existingBCollateralPhoto);
            }

            existingCollateral.setCif(originalCif);
            existingCollateral.setCollateralType(originalCollateralType);

            Collateral updatedCollateral = collateralRepository.save(existingCollateral);
            return modelMapper.map(updatedCollateral, CollateralDTO.class);
        } catch (Exception e) {
            throw new CollateralUpdateException(
                    "Failed to update collateral with id: " + id, e);
        }
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
        if (!collateralRepository.existsById(id)) {
            throw new CollateralNotFoundException(id);
        }
        try {
            Collateral collateral = collateralRepository.findById(id).get();
            collateral.setStatus(2);
            collateralRepository.save(collateral);
            return true;
        } catch (Exception e) {
            throw new CollateralUpdateException(
                    "Failed to soft delete collateral with id: " + id, e);
        }
    }

    @Transactional
    @Override
    public boolean restoreCollateral(Long id) {
        if (!collateralRepository.existsById(id)) {
            throw new CollateralNotFoundException(id);
        }
        try {
            Collateral collateral = collateralRepository.findById(id).get();
            collateral.setStatus(1);
            collateralRepository.save(collateral);
            return true;
        } catch (Exception e) {
            throw new CollateralUpdateException(
                    "Failed to restore collateral with id: " + id, e);
        }
    }

    @Override
    public Page<CollateralDTO> getAllCollateralsPaginated(
            Pageable pageable) {
        return collateralRepository.findAll(pageable)
                .map(collateral -> modelMapper.map(collateral, CollateralDTO.class));
    }

    @Override
    public List<CollateralDTO> getCollateralsByCifId(Long cifId) {
        return collateralRepository.findByCifIdAndStatus(cifId, 1).stream() // Fetch active collaterals only
                .map(collateral -> modelMapper.map(collateral, CollateralDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal getTotalCollateralValue() {
        return collateralRepository.sumCollateralValue().orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal getAverageCollateralPerLoan() {
        BigDecimal totalValue = getTotalCollateralValue();
        long loanCount = collateralRepository.countDistinctLoans();
        return loanCount > 0 ? totalValue.divide(BigDecimal.valueOf(loanCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    @Override
    public List<CollateralDTO> getAllCollateral(Long branchId) {
        List<Collateral> collaterals = collateralRepository.findActiveCollateral(branchId);
        return collaterals.stream()
                .map(collateral -> modelMapper.map(collaterals, CollateralDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CollateralDTO> getFreeezeCurrentAccountsByBranch(Long branchId) {
        List<Collateral> collaterals = collateralRepository.findInActiveCollateral(branchId);
        return collaterals.stream()
                .map(collateral -> modelMapper.map(collaterals, CollateralDTO.class))
                .collect(Collectors.toList());
    }

}

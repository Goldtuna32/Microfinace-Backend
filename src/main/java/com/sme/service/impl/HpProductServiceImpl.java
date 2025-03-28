package com.sme.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.sme.dto.HpProductDTO;
import com.sme.entity.HpProduct;
import com.sme.entity.ProductType;
import com.sme.entity.DealerRegistration;
import com.sme.exception.*;
import com.sme.repository.HpProductRepository;
import com.sme.repository.ProductTypeRepository;
import com.sme.repository.DealerRegistrationRepository;
import com.sme.service.HpProductService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HpProductServiceImpl implements HpProductService {

    @Autowired
    private HpProductRepository hpProductRepository;

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Autowired
    private DealerRegistrationRepository dealerRegistrationRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public List<HpProductDTO> getAllHpProducts() {
        List<HpProduct> hpProducts = hpProductRepository.findAllActive();
        return hpProducts.stream()
                .map(hpProduct -> modelMapper.map(hpProduct, HpProductDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public HpProductDTO getHpProductById(Long id) {
        Optional<HpProduct> hpProduct = hpProductRepository.findById(id);
        if (hpProduct.isEmpty()) {
            throw new HpProductNotFoundException(id);
        }
        return modelMapper.map(hpProduct.get(), HpProductDTO.class);
    }

    @Override
    @Transactional
    public HpProductDTO createHpProduct(HpProductDTO hpProductDTO) {
        validateHpProductDTO(hpProductDTO);

        if (hpProductRepository.existsByNameAndDealerRegistrationId(
                hpProductDTO.getName(), hpProductDTO.getDealerRegistrationId())) {
            throw new DuplicateHpProductException(hpProductDTO.getName(), hpProductDTO.getDealerRegistrationId());
        }

        try {
            HpProduct hpProduct = modelMapper.map(hpProductDTO, HpProduct.class);
            hpProduct.setStatus(1); // Default active status

            ProductType productType = productTypeRepository.findById(hpProductDTO.getProductTypeId())
                    .orElseThrow(() -> new RuntimeException("ProductType not found with ID: " + hpProductDTO.getProductTypeId()));
            hpProduct.setProductType(productType);

            DealerRegistration dealerRegistration = dealerRegistrationRepository.findById(hpProductDTO.getDealerRegistrationId())
                    .orElseThrow(() -> new DealerNotFoundException(hpProductDTO.getDealerRegistrationId()));
            hpProduct.setDealerRegistration(dealerRegistration);

            HpProduct savedHpProduct = hpProductRepository.save(hpProduct);
            return modelMapper.map(savedHpProduct, HpProductDTO.class);
        } catch (Exception e) {
            throw new HpProductCreationException(
                    "Failed to create HP product: " + hpProductDTO.getName(), e);
        }
    }

    public String uploadImage(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return uploadResult.get("url").toString();
        } catch (IOException e) {
            throw new ImageUploadException("Failed to upload image to Cloudinary", e);
        }
    }

    @Override
    @Transactional
    public HpProductDTO updateHpProduct(Long id, HpProductDTO hpProductDTO, MultipartFile photo) {
        HpProduct hpProduct = hpProductRepository.findById(id)
                .orElseThrow(() -> new HpProductNotFoundException(id));

        validateHpProductDTO(hpProductDTO);

        // Check for duplicate, excluding current product
        if (hpProductDTO.getName() != null &&
                !hpProductDTO.getName().equals(hpProduct.getName()) &&
                hpProductRepository.existsByNameAndDealerRegistrationId(
                        hpProductDTO.getName(), hpProductDTO.getDealerRegistrationId())) {
            throw new DuplicateHpProductException(hpProductDTO.getName(), hpProductDTO.getDealerRegistrationId());
        }

        try {
            String oldPhotoUrl = hpProduct.getHpProductPhoto();

            hpProduct.setName(hpProductDTO.getName());
            hpProduct.setPrice(hpProductDTO.getPrice());
            hpProduct.setCommissionFee(hpProductDTO.getCommissionFee());

            ProductType productType = productTypeRepository.findById(hpProductDTO.getProductTypeId())
                    .orElseThrow(() -> new RuntimeException("ProductType not found with ID: " + hpProductDTO.getProductTypeId()));
            hpProduct.setProductType(productType);

            DealerRegistration dealerRegistration = dealerRegistrationRepository.findById(hpProductDTO.getDealerRegistrationId())
                    .orElseThrow(() -> new DealerNotFoundException(hpProductDTO.getDealerRegistrationId()));
            hpProduct.setDealerRegistration(dealerRegistration);

            if (photo != null && !photo.isEmpty()) {
                deleteImage(oldPhotoUrl);
                String newPhotoUrl = uploadImage(photo);
                hpProduct.setHpProductPhoto(newPhotoUrl);
            }

            HpProduct updatedHpProduct = hpProductRepository.save(hpProduct);
            return modelMapper.map(updatedHpProduct, HpProductDTO.class);
        } catch (Exception e) {
            throw new HpProductUpdateException(
                    "Failed to update HP product with id: " + id, e);
        }
    }

    private void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }
        try {
            String publicId = imageUrl.substring(imageUrl.lastIndexOf("/") + 1, imageUrl.lastIndexOf("."));
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new HpProductUpdateException("Failed to delete image: " + imageUrl, e);
        }
    }

    @Override
    @Transactional
    public void deleteHpProduct(Long id) {
        if (!hpProductRepository.existsById(id)) {
            throw new HpProductNotFoundException(id);
        }
        try {
            hpProductRepository.softDelete(id);
        } catch (Exception e) {
            throw new HpProductUpdateException(
                    "Failed to soft delete HP product with id: " + id, e);
        }
    }

    @Override
    @Transactional
    public HpProductDTO restoreHpProduct(Long id) {
        HpProduct hpProduct = hpProductRepository.findById(id)
                .orElseThrow(() -> new HpProductNotFoundException(id));

        if (hpProduct.getStatus() == 1) {
            throw new HpProductValidationException(
                    "HP Product with id: " + id + " is already active");
        }

        try {
            hpProduct.setStatus(1);
            HpProduct restoredHpProduct = hpProductRepository.save(hpProduct);
            return modelMapper.map(restoredHpProduct, HpProductDTO.class);
        } catch (Exception e) {
            throw new HpProductUpdateException(
                    "Failed to restore HP product with id: " + id, e);
        }
    }

    private void validateHpProductDTO(HpProductDTO hpProductDTO) {
        // Required field validation
        if (hpProductDTO.getName() == null || hpProductDTO.getName().trim().isEmpty()) {
            throw new MissingRequiredFieldException("name");
        }
        if (hpProductDTO.getPrice() == null) {
            throw new MissingRequiredFieldException("price");
        }
        if (hpProductDTO.getCommissionFee() == null) {
            throw new MissingRequiredFieldException("commissionFee");
        }
        if (hpProductDTO.getProductTypeId() == null) {
            throw new MissingRequiredFieldException("productTypeId");
        }
        if (hpProductDTO.getDealerRegistrationId() == null) {
            throw new MissingRequiredFieldException("dealerRegistrationId");
        }

        // Price and commission fee validation
        if (hpProductDTO.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPriceException(hpProductDTO.getPrice());
        }
        if (hpProductDTO.getCommissionFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidCommissionFeeException(hpProductDTO.getCommissionFee());
        }

        // Name length validation (example: max 100 characters)
        if (hpProductDTO.getName().length() > 100) {
            throw new HpProductValidationException(
                    "Product name exceeds maximum length of 100 characters");
        }
    }

    @Override
    public List<Object[]> countProductsByType() {
        return hpProductRepository.countProductsGroupedByType();
    }


}

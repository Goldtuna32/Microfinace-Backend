package com.sme.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.sme.dto.HpProductDTO;
import com.sme.entity.HpProduct;
import com.sme.entity.ProductType;
import com.sme.entity.DealerRegistration;
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
        return hpProduct.map(hp -> modelMapper.map(hp, HpProductDTO.class)).orElse(null);
    }

    @Override
    public HpProductDTO createHpProduct(HpProductDTO hpProductDTO) {
        HpProduct hpProduct = modelMapper.map(hpProductDTO, HpProduct.class);
        hpProduct.setStatus(1); // Default active status

        // Set related entities
        ProductType productType = productTypeRepository.findById(hpProductDTO.getProductTypeId())
                .orElseThrow(() -> new RuntimeException("ProductType not found"));
        hpProduct.setProductType(productType);

        DealerRegistration dealerRegistration = dealerRegistrationRepository.findById(hpProductDTO.getDealerRegistrationId())
                .orElseThrow(() -> new RuntimeException("DealerRegistration not found"));
        hpProduct.setDealerRegistration(dealerRegistration);

        // Photo URL is already set in the DTO by the controller
        HpProduct savedHpProduct = hpProductRepository.save(hpProduct);
        return modelMapper.map(savedHpProduct, HpProductDTO.class);
    }

    public String uploadImage(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return uploadResult.get("url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image to Cloudinary", e);
        }
    }

    @Override
    public HpProductDTO updateHpProduct(Long id, HpProductDTO hpProductDTO, MultipartFile photo) {
        Optional<HpProduct> existingHpProductOpt = hpProductRepository.findById(id);
        if (existingHpProductOpt.isEmpty()) {
            System.out.println("Product not found for ID: " + id);
            return null;
        }

        HpProduct hpProduct = existingHpProductOpt.get();
        System.out.println("Before updating - ID: " + hpProduct.getId()); // Should be 5
        String oldPhotoUrl = hpProduct.getHpProductPhoto();

        // Manually update fields instead of using ModelMapper
        hpProduct.setName(hpProductDTO.getName());
        hpProduct.setPrice(hpProductDTO.getPrice());
        hpProduct.setCommissionFee(hpProductDTO.getCommissionFee());
        // Status is not updated here; assuming it’s managed separately (e.g., delete/restore)

        // Update related entities
        ProductType productType = productTypeRepository.findById(hpProductDTO.getProductTypeId())
                .orElseThrow(() -> new RuntimeException("ProductType not found"));
        hpProduct.setProductType(productType);

        DealerRegistration dealerRegistration = dealerRegistrationRepository.findById(hpProductDTO.getDealerRegistrationId())
                .orElseThrow(() -> new RuntimeException("DealerRegistration not found"));
        hpProduct.setDealerRegistration(dealerRegistration);

        // Handle photo update
        if (photo != null && !photo.isEmpty()) {
            deleteImage(oldPhotoUrl);
            String newPhotoUrl = uploadImage(photo);
            hpProduct.setHpProductPhoto(newPhotoUrl);
        }

        System.out.println("Before save - ID: " + hpProduct.getId()); // Should still be 5
        HpProduct updatedHpProduct = hpProductRepository.save(hpProduct);
        System.out.println("After save - ID: " + updatedHpProduct.getId()); // Should still be 5

        return modelMapper.map(updatedHpProduct, HpProductDTO.class);
    }

    private void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }
        try {
            String publicId = imageUrl.substring(imageUrl.lastIndexOf("/") + 1, imageUrl.lastIndexOf("."));
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            System.err.println("Failed to delete image: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteHpProduct(Long id) {
        hpProductRepository.softDelete(id);
    }

    @Override
    @Transactional
    public HpProductDTO restoreHpProduct(Long id) {
        Optional<HpProduct> hpProductOpt = hpProductRepository.findById(id);
        if (hpProductOpt.isPresent()) {
            HpProduct hpProduct = hpProductOpt.get();
            if (hpProduct.getStatus() == 1) {
                throw new RuntimeException("HP Product with id: " + id + " is already active");
            }
            hpProduct.setStatus(1); // Restore to active
            HpProduct restoredHpProduct = hpProductRepository.save(hpProduct);
            return modelMapper.map(restoredHpProduct, HpProductDTO.class);
        }
        return null;
    }


}

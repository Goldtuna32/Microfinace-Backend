package com.sme.service.impl;

import com.sme.dto.ProductTypeDTO;
import com.sme.entity.ProductType;
import com.sme.exception.*;
import com.sme.repository.ProductTypeRepository;
import com.sme.service.ProductTypeService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductTypeServiceImpl implements ProductTypeService {

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<ProductTypeDTO> getAllProductTypes() {
        List<ProductType> productTypes = productTypeRepository.findAllActive();
        return productTypes.stream()
                .map(productType -> modelMapper.map(productType, ProductTypeDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public ProductTypeDTO getProductTypeById(Long id) {
        Optional<ProductType> productType = productTypeRepository.findById(id);
        return productType.map(pt -> modelMapper.map(pt, ProductTypeDTO.class)).orElse(null);
    }

    @Override
    @Transactional
    public ProductTypeDTO createProductType(ProductTypeDTO productTypeDTO) {
        validateProductTypeDTO(productTypeDTO);

        if (productTypeRepository.existsByName(productTypeDTO.getName())) {
            throw new DuplicateProductTypeException(productTypeDTO.getName());
        }

        try {
            ProductType productType = modelMapper.map(productTypeDTO, ProductType.class);
            productType.setStatus(1); // Default active status
            ProductType savedProductType = productTypeRepository.save(productType);
            return modelMapper.map(savedProductType, ProductTypeDTO.class);
        } catch (Exception e) {
            throw new ProductTypeCreationException(
                    "Failed to create product type: " + productTypeDTO.getName(), e);
        }
    }

    @Override
    public ProductTypeDTO updateProductType(Long id, ProductTypeDTO productTypeDTO) {
        ProductType productType = productTypeRepository.findById(id)
                .orElseThrow(() -> new ProductTypeNotFoundException(id));

        validateProductTypeDTO(productTypeDTO);

        // Check for duplicate name, excluding current product type
        if (productTypeDTO.getName() != null &&
                !productTypeDTO.getName().equals(productType.getName()) &&
                productTypeRepository.existsByName(productTypeDTO.getName())) {
            throw new DuplicateProductTypeException(productTypeDTO.getName());
        }

        try {
            modelMapper.map(productTypeDTO, productType);
            ProductType updatedProductType = productTypeRepository.save(productType);
            return modelMapper.map(updatedProductType, ProductTypeDTO.class);
        } catch (Exception e) {
            throw new ProductTypeUpdateException(
                    "Failed to update product type with id: " + id, e);
        }
    }

    @Override
    @Transactional
    public ProductTypeDTO restoreProductType(Long id) {
        ProductType productType = productTypeRepository.findById(id)
                .orElseThrow(() -> new ProductTypeNotFoundException(id));

        if (productType.getStatus() == 1) {
            throw new InvalidStatusException("Product type with id: " + id + " is already active");
        }

        try {
            productType.setStatus(1);
            ProductType restored = productTypeRepository.save(productType);
            return modelMapper.map(restored, ProductTypeDTO.class);
        } catch (Exception e) {
            throw new ProductTypeUpdateException(
                    "Failed to restore product type with id: " + id, e);
        }
    }

    @Override
    @Transactional
    public void deleteProductType(Long id) {
        if (!productTypeRepository.existsById(id)) {
            throw new ProductTypeNotFoundException(id);
        }
        try {
            productTypeRepository.softDelete(id);
        } catch (Exception e) {
            throw new ProductTypeUpdateException(
                    "Failed to soft delete product type with id: " + id, e);
        }
    }

    private void validateProductTypeDTO(ProductTypeDTO productTypeDTO) {
        if (productTypeDTO.getName() == null || productTypeDTO.getName().trim().isEmpty()) {
            throw new MissingRequiredFieldException("name");
        }

        // Name length validation (example: max 100 characters)
        if (productTypeDTO.getName().length() > 100) {
            throw new ProductTypeValidationException(
                    "Product type name exceeds maximum length of 100 characters");
        }

        // Status validation (if provided)
        if (productTypeDTO.getStatus() != null &&
                productTypeDTO.getStatus() != 1 &&
                productTypeDTO.getStatus() != 2) {
            throw new InvalidStatusException(productTypeDTO.getStatus());
        }
    }
    @Override
    public List<ProductTypeDTO> getActiveProductTypesByBranch(Long branchId) {
        List<ProductType> productTypes = productTypeRepository.findActiveProductTypesByBranchId(branchId);
        return convertProductTypesToDTOs(productTypes);
    }

    @Override
    public List<ProductTypeDTO> getInActiveProductTypesByBranch(Long branchId) {
        List<ProductType> productTypes = productTypeRepository.findInActiveProductTypesByBranchId(branchId);
        return convertProductTypesToDTOs(productTypes);
    }

    private List<ProductTypeDTO> convertProductTypesToDTOs(List<ProductType> productTypes) {
        List<ProductTypeDTO> dtos = new ArrayList<>();
        for (ProductType productType : productTypes) {
            dtos.add(convertToProductTypeDTO(productType));
        }
        return dtos;
    }

    private ProductTypeDTO convertToProductTypeDTO(ProductType productType) {
        ProductTypeDTO dto = new ProductTypeDTO();
        dto.setId(productType.getId());
        dto.setName(productType.getName());
        dto.setStatus(productType.getStatus());
        return dto;
    }
}

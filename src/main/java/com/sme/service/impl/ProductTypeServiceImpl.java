package com.sme.service.impl;

import com.sme.dto.ProductTypeDTO;
import com.sme.entity.ProductType;
import com.sme.repository.ProductTypeRepository;
import com.sme.service.ProductTypeService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public ProductTypeDTO createProductType(ProductTypeDTO productTypeDTO) {
        ProductType productType = modelMapper.map(productTypeDTO, ProductType.class);
        productType.setStatus(1); // Assuming 1 is the status for active
        ProductType savedProductType = productTypeRepository.save(productType);
        return modelMapper.map(savedProductType, ProductTypeDTO.class);
    }

    @Override
    public ProductTypeDTO updateProductType(Long id, ProductTypeDTO productTypeDTO) {
        Optional<ProductType> existingProductType = productTypeRepository.findById(id);
        if (existingProductType.isPresent()) {
            ProductType productType = existingProductType.get();
            modelMapper.map(productTypeDTO, productType);
            ProductType updatedProductType = productTypeRepository.save(productType);
            return modelMapper.map(updatedProductType, ProductTypeDTO.class);
        }
        return null;
    }

    @Override
    @Transactional
    public ProductTypeDTO restoreProductType(Long id) {
        ProductType productType = productTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductType not found with id: " + id));
        if (productType.getStatus() == 1) {
            throw new RuntimeException("ProductType with id: " + id + " is already active");
        }
        productType.setStatus(1); // Restore to active
        ProductType restored = productTypeRepository.save(productType);
        return modelMapper.map(restored, ProductTypeDTO.class);
    }

    @Override
    @Transactional
    public void deleteProductType(Long id) {
        productTypeRepository.softDelete(id);
    }
}

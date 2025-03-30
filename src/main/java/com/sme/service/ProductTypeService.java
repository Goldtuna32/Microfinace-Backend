package com.sme.service;

import com.sme.dto.ProductTypeDTO;
import com.sme.entity.ProductType;

import java.util.List;

public interface ProductTypeService {
    List<ProductTypeDTO> getAllProductTypes();
    ProductTypeDTO getProductTypeById(Long id);
    ProductTypeDTO createProductType(ProductTypeDTO productTypeDTO);
    ProductTypeDTO updateProductType(Long id, ProductTypeDTO productTypeDTO);
    void deleteProductType(Long id);
    ProductTypeDTO restoreProductType(Long id);

    List<ProductTypeDTO> getInActiveProductTypesByBranch(Long branchId);
    List<ProductTypeDTO> getActiveProductTypesByBranch(Long branchId);
}

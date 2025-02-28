package com.sme.service;

import com.sme.dto.ProductTypeDTO;
import com.sme.entity.ProductType;
import com.sme.entity.Status;
import com.sme.repository.ProductTypeRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;


public interface ProductTypeService {


    ProductTypeDTO createProductType(ProductTypeDTO productTypeDTO);

    List<ProductTypeDTO> getAllActiveProductTypes();

    ProductTypeDTO getProductTypeById(Long id);

    ProductTypeDTO updateProductType(Long id, ProductTypeDTO productTypeDTO);

    void deleteProductType(Long id);
}

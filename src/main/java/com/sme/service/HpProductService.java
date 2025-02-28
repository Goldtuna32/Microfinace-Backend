package com.sme.service;

import com.sme.dto.HpProductDTO;
import com.sme.entity.HpProduct;
import com.sme.entity.DealerRegistration;
import com.sme.entity.ProductType;
import com.sme.repository.HpProductRepository;
import com.sme.repository.DealerRegistrationRepository;
import com.sme.repository.ProductTypeRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


public interface HpProductService {

    HpProductDTO createHpProduct(HpProductDTO productDTO);

    List<HpProductDTO> getAllProducts();

}

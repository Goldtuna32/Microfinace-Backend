package com.sme.service;

import com.sme.dto.HpProductDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface HpProductService {
    List<HpProductDTO> getAllHpProducts();
    HpProductDTO getHpProductById(Long id);
    HpProductDTO createHpProduct(HpProductDTO hpProductDTO);
    HpProductDTO updateHpProduct(Long id, HpProductDTO hpProductDTO, MultipartFile photo);
    void deleteHpProduct(Long id);
    HpProductDTO restoreHpProduct(Long id);
    String uploadImage(MultipartFile file);
}

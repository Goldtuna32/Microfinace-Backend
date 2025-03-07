package com.sme.service;

import com.sme.entity.CollateralType;
import java.util.List;

public interface CollateralTypeService {
    CollateralType createCollateralType(CollateralType collateralType);
    CollateralType getCollateralTypeById(Long id);
    List<CollateralType> getAllActiveCollateralTypes();
    List<CollateralType> getAllDeletedCollateralTypes();
    CollateralType updateCollateralType(Long id, CollateralType collateralType);
    void softDeleteCollateralType(Long id);
}
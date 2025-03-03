package com.sme.service.impl;

import com.sme.entity.CollateralType;
import com.sme.repository.CollateralTypeRepository;
import com.sme.service.CollateralTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CollateralTypeServiceImpl implements CollateralTypeService {

    @Autowired
    private CollateralTypeRepository repository;

    @Override
    public CollateralType createCollateralType(CollateralType collateralType) {
        collateralType.setStatus(1); // Default new records to active
        return repository.save(collateralType);
    }

    @Override
    public CollateralType getCollateralTypeById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public List<CollateralType> getAllActiveCollateralTypes() {
        return repository.findByStatus(1); // Only return active records
    }

    @Override
    public List<CollateralType> getAllDeletedCollateralTypes() {
        return repository.findByStatus(2);
    }

    @Override
    public CollateralType updateCollateralType(Long id, CollateralType collateralType) {
        if (repository.existsById(id)) {
            collateralType.setId(id);
            return repository.save(collateralType);
        }
        return null;
    }

    @Override
    public void softDeleteCollateralType(Long id) {
        Optional<CollateralType> optionalCollateralType = repository.findById(id);
        if (optionalCollateralType.isPresent()) {
            CollateralType collateralType = optionalCollateralType.get();
            collateralType.setStatus(2); // Mark as inactive
            repository.save(collateralType);
        }
    }
}
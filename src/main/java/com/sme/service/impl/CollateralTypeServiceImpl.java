package com.sme.service.impl;

import com.sme.entity.CollateralType;
import com.sme.exception.*;
import com.sme.repository.CollateralTypeRepository;
import com.sme.service.CollateralTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CollateralTypeServiceImpl implements CollateralTypeService {

    @Autowired
    private CollateralTypeRepository repository;

    @Override
    public CollateralType createCollateralType(CollateralType collateralType) {
        validateCollateralType(collateralType);

        if (collateralType.getName() != null && repository.existsByName(collateralType.getName())) {
            throw new DuplicateCollateralTypeException(collateralType.getName());
        }

        try {
            collateralType.setStatus(1); // Default new records to active
            return repository.save(collateralType);
        } catch (Exception e) {
            throw new CollateralTypeCreationException(
                    "Failed to create collateral type: " + collateralType.getName(), e);
        }
    }

    @Override
    public CollateralType getCollateralTypeById(Long id) {
        Optional<CollateralType> collateralType = repository.findById(id);
        if (collateralType.isEmpty()) {
            throw new CollateralTypeNotFoundException(id);
        }
        return collateralType.get();
    }

    @Override
    public List<CollateralType> getAllActiveCollateralTypes() {
        return repository.findByStatus(1);
    }

    @Override
    public List<CollateralType> getAllDeletedCollateralTypes() {
        return repository.findByStatus(2);
    }

    @Override
    public CollateralType updateCollateralType(Long id, CollateralType collateralType) {
        if (!repository.existsById(id)) {
            throw new CollateralTypeNotFoundException(id);
        }

        validateCollateralType(collateralType);

        // Check for duplicate name, excluding the current ID
        if (collateralType.getName() != null &&
                repository.existsByNameAndIdNot(collateralType.getName(), id)) {
            throw new DuplicateCollateralTypeException(collateralType.getName());
        }

        try {
            collateralType.setId(id);
            return repository.save(collateralType);
        } catch (Exception e) {
            throw new CollateralTypeUpdateException(
                    "Failed to update collateral type with id: " + id, e);
        }
    }

    @Override
    public void softDeleteCollateralType(Long id) {
        Optional<CollateralType> optionalCollateralType = repository.findById(id);
        if (optionalCollateralType.isEmpty()) {
            throw new CollateralTypeNotFoundException(id);
        }

        try {
            CollateralType collateralType = optionalCollateralType.get();
            collateralType.setStatus(2); // Mark as inactive
            repository.save(collateralType);
        } catch (Exception e) {
            throw new CollateralTypeUpdateException(
                    "Failed to soft delete collateral type with id: " + id, e);
        }
    }

    private void validateCollateralType(CollateralType collateralType) {
        // Required field validation
        if (collateralType.getName() == null || collateralType.getName().trim().isEmpty()) {
            throw new MissingRequiredFieldException("name");
        }

        // Name length validation (example: max 100 characters)
        if (collateralType.getName().length() > 100) {
            throw new CollateralTypeValidationException(
                    "Name exceeds maximum length of 100 characters");
        }

        // Status validation
        if (collateralType.getStatus() != null &&
                collateralType.getStatus() != 1 &&
                collateralType.getStatus() != 2) {
            throw new InvalidStatusException(collateralType.getStatus());
        }
    }
}
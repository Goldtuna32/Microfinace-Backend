package com.sme.repository;

import com.sme.entity.ProductType;

import com.sme.entity.Status;
 
 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {

    List<ProductType> findByStatus(Status status);
}

package com.sme.repository;

import com.sme.entity.ProductType;

import com.sme.entity.Status;
 
 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {

    List<ProductType> findByStatus(Status status);

    @Query("SELECT p FROM ProductType p WHERE p.status != 0")
    List<ProductType> findAllActive();

    @Modifying
    @Query("UPDATE ProductType p SET p.status = 0 WHERE p.id = :id")
    void softDelete(@Param("id") Long id);
}

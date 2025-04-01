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
    @Query("UPDATE ProductType p SET p.status = 2 WHERE p.id = :id")
    void softDelete(@Param("id") Long id);

    boolean existsByName(String name);

    @Query("SELECT DISTINCT pt FROM ProductType pt " +
            "JOIN pt.hpProducts hp " +
            "JOIN hp.dealerRegistration dr " +
            "JOIN dr.currentAccount.cif.branch b " +
            "WHERE pt.status = 1 AND hp.status = 1 AND dr.status = 1 " +
            "AND (:branchId IS NULL OR b.id = :branchId)")
    List<ProductType> findActiveProductTypesByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT DISTINCT pt FROM ProductType pt " +
            "JOIN pt.hpProducts hp " +
            "JOIN hp.dealerRegistration dr " +
            "JOIN dr.currentAccount.cif.branch b " +
            "WHERE pt.status = 2 AND hp.status = 1 AND dr.status = 1 " +
            "AND (:branchId IS NULL OR b.id = :branchId)")
    List<ProductType> findInActiveProductTypesByBranchId(@Param("branchId") Long branchId);
}

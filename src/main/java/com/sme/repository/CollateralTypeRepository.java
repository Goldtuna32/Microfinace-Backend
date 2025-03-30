package com.sme.repository;

import com.sme.entity.CollateralType;
import com.sme.entity.CurrentAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CollateralTypeRepository extends JpaRepository<CollateralType, Long> {
    List<CollateralType> findByStatus(Integer status);

    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);

    @Query("SELECT DISTINCT ct FROM CollateralType ct " +
            "JOIN Collateral c ON c.collateralType.id = ct.id " +
            "JOIN c.cif cif " +
            "JOIN cif.branch b " +
            "WHERE ct.status = 1 AND c.status = 1 " +
            "AND (:branchId IS NULL OR b.id = :branchId)")
    List<CollateralType> findActiveCollateralTypesByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT DISTINCT ct FROM CollateralType ct " +
            "JOIN Collateral c ON c.collateralType.id = ct.id " +
            "JOIN c.cif cif " +
            "JOIN cif.branch b " +
            "WHERE ct.status = 2 AND c.status = 1 " +
            "AND (:branchId IS NULL OR b.id = :branchId)")
    List<CollateralType> findInActiveCollateralTypesByBranchId(@Param("branchId") Long branchId);
}
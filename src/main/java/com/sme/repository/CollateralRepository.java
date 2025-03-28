package com.sme.repository;

import com.sme.entity.Collateral;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CollateralRepository extends JpaRepository<Collateral, Long> {
    Optional<Collateral> findTopByOrderByIdDesc();

    @Query("SELECT c FROM Collateral c WHERE c.status = :status")
    List<Collateral> findByStatus(@Param("status") Integer status);

    Page<Collateral> findAll(Pageable pageable);

    List<Collateral> findByCifIdAndStatus(Long cifId, Integer status);

    @Query("SELECT SUM(c.value) FROM Collateral c WHERE c.status = 1")
    Optional<BigDecimal> sumCollateralValue();

    @Query("SELECT COUNT(DISTINCT slc.smeLoan.id) FROM SmeLoanCollateral slc")
    int countDistinctLoans();

    @Query("SELECT COUNT(c) FROM Collateral c WHERE c.cif.branch.id = :branchId")
    int countByBranchId(Long branchId);

}

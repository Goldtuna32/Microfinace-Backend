package com.sme.repository;

import com.sme.entity.CIF;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CIFRepository extends JpaRepository<CIF, Long> {

    @Query("SELECT c.serialNumber FROM CIF c WHERE c.branch.id = :branchId ORDER BY c.createdAt DESC LIMIT 1")
    String findLastSerialNumberByBranch(@Param("branchId") Long branchId);

    List<CIF> findByStatus(Integer status);

    @Query("SELECT c FROM CIF c WHERE c.status = 1 " +
            "AND (:nrcPrefix IS NULL OR c.nrcNumber LIKE :nrcPrefix%)")
    Page<CIF> findActiveCIFs(@Param("nrcPrefix") String nrcPrefix, Pageable pageable);

    @Query("SELECT c FROM CIF c WHERE c.status = 2 " +
            "AND (:nrcPrefix IS NULL OR c.nrcNumber LIKE :nrcPrefix%)")
    Page<CIF> findDeletedCIFs(@Param("nrcPrefix") String nrcPrefix, Pageable pageable);

    @Query("SELECT c.serialNumber FROM CIF c WHERE c.branch.branchCode = :branchCode ORDER BY c.serialNumber DESC LIMIT 1")
    String findLastCifCodeByBranchCode(@Param("branchCode") String branchCode);

    boolean existsByName(String name);
    boolean existsByNrcNumber(String nrcNumber);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);

}

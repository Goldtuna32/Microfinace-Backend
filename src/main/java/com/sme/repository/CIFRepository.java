package com.sme.repository;

import com.sme.entity.CIF;
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
}

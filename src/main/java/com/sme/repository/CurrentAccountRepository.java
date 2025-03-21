package com.sme.repository;

import com.sme.entity.CurrentAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrentAccountRepository extends JpaRepository<CurrentAccount, Long> {
    boolean existsByCifId(Long cifId);
    Page<CurrentAccount> findAll(Pageable pageable);
//    @Query("SELECT ca FROM CurrentAccount ca WHERE ca.cif.id = :cifId")
//    CurrentAccount findByCifId(Long cifId);

    Optional<CurrentAccount> findByCifId(Long cifId);

    @Query("SELECT ca.accountNumber FROM CurrentAccount ca WHERE ca.cif.branch.branchCode = :branchCode ORDER BY ca.accountNumber DESC LIMIT 1")
    String findLastAccountNumberByBranchCode(String branchCode);

    @Query("SELECT ca FROM CurrentAccount ca WHERE ca.cif.serialNumber = :serialNumber")
    List<CurrentAccount> findByCifSerialNumber(String serialNumber);
}


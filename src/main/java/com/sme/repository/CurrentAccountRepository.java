package com.sme.repository;

import com.sme.entity.CIF;
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

    @Query("SELECT COUNT(ca) FROM CurrentAccount ca WHERE ca.cif.branch.id = :branchId")
    int countByBranchId(Long branchId);

//    @Query("SELECT ca FROM CurrentAccount ca JOIN ca.dealer d JOIN d.products p WHERE p.id = :productId")
//    Optional<CurrentAccount> findDealerAccountByProductId(@Param("productId") Long productId);

    @Query("SELECT ca FROM CurrentAccount ca WHERE ca.status = 1 " +
            "AND (:branchId IS NULL OR ca.cif.branch.id = :branchId)")
    List<CurrentAccount> findActiveCurrentAccount(
            @Param("branchId") Long branchId);

    @Query("SELECT ca FROM CurrentAccount ca WHERE ca.status = 2 " +
            "AND (:branchId IS NULL OR ca.cif.branch.id = :branchId)")
    List<CurrentAccount> findFreezeCurrentAccount(
            @Param("branchId") Long branchId);

    @Query("SELECT dr.currentAccount FROM DealerRegistration dr " +
            "JOIN dr.hpProducts hp " +
            "WHERE hp.id = :productId")
    Optional<CurrentAccount> findDealerAccountByHpProductId(@Param("productId") Long productId);

}


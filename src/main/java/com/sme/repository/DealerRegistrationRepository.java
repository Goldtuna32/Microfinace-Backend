package com.sme.repository;

import com.sme.entity.DealerRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DealerRegistrationRepository extends JpaRepository<DealerRegistration, Long> {
  
     List<DealerRegistration> findByStatus(Integer status);

     boolean existsByCompanyName(String companyName);

     boolean existsByPhoneNumber(String phoneNumber);

     @Query("SELECT COUNT(dr) FROM DealerRegistration dr WHERE dr.currentAccount.cif.branch.id = :branchId")
     int countByBranchId(Long branchId);

     @Query("SELECT d FROM DealerRegistration d WHERE d.status = 1 " +
             "AND (:branchId IS NULL OR d.currentAccount.cif.branch.id = :branchId)")
     List<DealerRegistration> findActiveByBranchId(
             @Param("branchId") Long branchId);

     @Query("SELECT d FROM DealerRegistration d WHERE d.status = 2 " +
             "AND (:branchId IS NULL OR d.currentAccount.cif.branch.id = :branchId)")
     List<DealerRegistration> findInactiveByBranchId(
             @Param("branchId") Long branchId);

     @Query("SELECT dr FROM DealerRegistration dr " +
             "JOIN FETCH dr.currentAccount " +
             "JOIN dr.hpProducts hp " +
             "WHERE hp.id = :productId")
     Optional<DealerRegistration> findByIdWithAccount(@Param("productId") Long productId);

}

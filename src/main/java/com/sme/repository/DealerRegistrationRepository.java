package com.sme.repository;

import com.sme.entity.DealerRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DealerRegistrationRepository extends JpaRepository<DealerRegistration, Long> {
  
     List<DealerRegistration> findByStatus(Integer status);

     boolean existsByCompanyName(String companyName);

     boolean existsByPhoneNumber(String phoneNumber);

     @Query("SELECT COUNT(dr) FROM DealerRegistration dr WHERE dr.currentAccount.cif.branch.id = :branchId")
     int countByBranchId(Long branchId);


}

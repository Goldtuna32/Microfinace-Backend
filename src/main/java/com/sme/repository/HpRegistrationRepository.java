package com.sme.repository;

import com.sme.entity.HpRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HpRegistrationRepository extends JpaRepository<HpRegistration, Long> {
    Long countByStatus(Integer status);

    @Query("SELECT MONTH(h.createdDate) - 1 as month, COUNT(h.id) as count " +
            "FROM HpRegistration h " +
            "WHERE YEAR(h.createdDate) = YEAR(CURRENT_DATE) " +
            "GROUP BY MONTH(h.createdDate) - 1")  // Match SELECT expression
    List<Object[]> countHpRegistrationsGroupedByMonth();

    @Query("SELECT COUNT(hr) FROM HpRegistration hr WHERE hr.currentAccount.cif.branch.id = :branchId")
    int countByBranchId(Long branchId);

    @Query("SELECT COUNT(hr) FROM HpRegistration hr WHERE hr.currentAccount.cif.branch.id = :branchId AND hr.status = :status")
    int countByBranchIdAndStatus(Long branchId, Integer status);
}

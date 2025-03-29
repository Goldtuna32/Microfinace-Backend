package com.sme.repository;

import com.sme.entity.CIF;
import com.sme.entity.CurrentAccount;
import com.sme.entity.SmeLoanRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmeLoanRegistrationRepository extends JpaRepository<SmeLoanRegistration, Long> {
    List<SmeLoanRegistration> findByStatus(Integer status);

    @Query("SELECT s FROM SmeLoanRegistration s WHERE s.status = 4")
    Page<SmeLoanRegistration> findAllActiveLoans(Pageable pageable);

    @Query("SELECT s FROM SmeLoanRegistration s WHERE s.status = 3")
    Page<SmeLoanRegistration> findAllPendingLoans(Pageable pageable);

    Long countByCurrentAccountId(Long currentAccountId);

    Long countByStatus(Integer status);

    @Query("SELECT MONTH(s.repaymentStartDate) - 1 as month, COUNT(s) as count " +
            "FROM SmeLoanRegistration s " +
            "WHERE YEAR(s.repaymentStartDate) = YEAR(CURRENT_DATE) " +
            "GROUP BY month")  // Group by the alias
    List<Object[]> countLoansGroupedByMonth();

    @Query("SELECT COUNT(slr) FROM SmeLoanRegistration slr WHERE slr.currentAccount.cif.branch.id = :branchId")
    int countByBranchId(Long branchId);

    @Query("SELECT COUNT(slr) FROM SmeLoanRegistration slr WHERE slr.currentAccount.cif.branch.id = :branchId AND slr.status = :status")
    int countByBranchIdAndStatus(Long branchId, Integer status);

    @Query("SELECT s FROM SmeLoanRegistration s WHERE s.status = 3 " +
            "AND (:branchId IS  NULL OR s.currentAccount.cif.branch.id = :branchId)")
    List<SmeLoanRegistration> findPendingLoans(
            @Param("branchId") Long branchId);

    @Query("SELECT s FROM SmeLoanRegistration s WHERE s.status = 4 " +
            "AND (:branchId IS NULL OR s.currentAccount.cif.branch.id = :branchId)")
    List<SmeLoanRegistration> findApprovedLoans(
            @Param("branchId") Long branchId);
}

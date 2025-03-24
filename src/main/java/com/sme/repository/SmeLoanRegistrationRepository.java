package com.sme.repository;

import com.sme.entity.CIF;
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
}

package com.sme.repository;

import com.sme.entity.SmeLoanCollateral;
import com.sme.entity.SmeLoanRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmeLoanCollateralRepository extends JpaRepository<SmeLoanCollateral, Long> {
    List<SmeLoanCollateral> findBySmeLoanId(Long smeLoanId);
    List<SmeLoanCollateral> findByCollateralId(Long collateralId);
    void deleteBySmeLoan(SmeLoanRegistration smeLoan);

}

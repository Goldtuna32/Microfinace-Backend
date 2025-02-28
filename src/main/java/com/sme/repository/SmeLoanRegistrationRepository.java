package com.sme.repository;

import com.sme.entity.SmeLoanRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmeLoanRegistrationRepository extends JpaRepository<SmeLoanRegistration, Long> {
    List<SmeLoanRegistration> findByStatus(Integer status);

}

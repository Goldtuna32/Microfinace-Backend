package com.sme.repository;

import com.sme.entity.HpProduct;
import com.sme.entity.HpRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HpRegistrationRepository extends JpaRepository<HpRegistration, Long> {

    Page<HpRegistration> findByStatus(Integer status, Pageable pageable);


}

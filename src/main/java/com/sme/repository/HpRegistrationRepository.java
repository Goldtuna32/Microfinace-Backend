package com.sme.repository;

import com.sme.entity.HpRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HpRegistrationRepository extends JpaRepository<HpRegistration, Long> {
}

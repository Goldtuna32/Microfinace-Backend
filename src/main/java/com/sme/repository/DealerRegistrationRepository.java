package com.sme.repository;

import com.sme.entity.DealerRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DealerRegistrationRepository extends JpaRepository<DealerRegistration, Long> {
  
     List<DealerRegistration> findByStatus(Integer status);

 
}

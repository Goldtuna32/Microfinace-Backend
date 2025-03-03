package com.sme.repository;

import com.sme.entity.Collateral;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollateralRepository extends JpaRepository<Collateral, Long> {
    Optional<Collateral> findTopByOrderByIdDesc();

    @Query("SELECT c FROM Collateral c WHERE c.status = :status")
    List<Collateral> findByStatus(@Param("status") Integer status);

    Page<Collateral> findAll(Pageable pageable);

}

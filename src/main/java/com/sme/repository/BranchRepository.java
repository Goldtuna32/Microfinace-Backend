package com.sme.repository;

import com.sme.entity.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    @Query("SELECT b.branchCode FROM Branch b ORDER BY b.branchCode DESC LIMIT 1")
    String findLastBranchCode();

    @Query("SELECT b.branchCode FROM Branch b " +
            "JOIN b.address a " +
            "WHERE a.region = :region AND TRIM(a.township) = :township " +
            "ORDER BY b.id DESC")
    String findLastBranchCodeByRegionAndTownship(@Param("region") String region,
                                                 @Param("township") String township);

    Page<Branch> findAll(Pageable pageable);



}

package com.sme.repository;

import com.sme.entity.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    @Query("SELECT b FROM Branch b " +
            "WHERE (:region IS NULL OR b.address.region = :region) " +
            "AND (:name IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:branchCode IS NULL OR LOWER(b.branchCode) LIKE LOWER(CONCAT('%', :branchCode, '%')))")
    Page<Branch> findBranches(
            @Param("region") String region,
            @Param("name") String name,
            @Param("branchCode") String branchCode,
            Pageable pageable);


    @Query("SELECT b.branchCode FROM Branch b WHERE b.address.region = :region ORDER BY b.branchCode DESC LIMIT 1")
    String findLastBranchCodeByRegion(@Param("region") String region);

    boolean existsByName(String Name);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);

}

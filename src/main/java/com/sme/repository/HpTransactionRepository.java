package com.sme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sme.entity.HpRepaymentTransaction;
@Repository
public interface HpTransactionRepository extends JpaRepository<HpRepaymentTransaction, Long> {
    // Add custom query methods if needed
}
package com.sme.repository;

import com.sme.entity.AccountTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Integer> {

    // Fetch latest transaction date for an account
    @Query("SELECT a FROM AccountTransaction a WHERE a.currentAccount.id = :accountId ORDER BY a.transactionDate DESC LIMIT 1")
    Optional<AccountTransaction> findLatestTransactionByAccount(@Param("accountId") Long accountId);
}


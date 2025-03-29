package com.sme.repository;

import com.sme.entity.AccountTransaction;
import jakarta.transaction.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Integer> {
    List<AccountTransaction> findByCurrentAccountId(Long accountId);

    @Query("SELECT COUNT(at) FROM AccountTransaction at WHERE at.currentAccount.cif.branch.id = :branchId")
    int countByBranchId(Long branchId);
}

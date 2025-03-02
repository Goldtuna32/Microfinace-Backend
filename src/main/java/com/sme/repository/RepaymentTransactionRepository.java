package com.sme.repository;

import com.sme.entity.RepaymentSchedule;
import com.sme.entity.RepaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RepaymentTransactionRepository extends JpaRepository<RepaymentTransaction, Long> {

    // Find transactions for a specific date
    List<RepaymentTransaction> findByPaymentDate(LocalDate paymentDate);

    // Find transactions related to a specific repayment schedule
    List<RepaymentTransaction> findByRepaymentScheduleId(Long repaymentScheduleId);

    // Find transactions related to a specific current account
    List<RepaymentTransaction> findByCurrentAccountId(Long currentAccountId);

    @Query("SELECT rs FROM RepaymentSchedule rs WHERE rs.dueDate = :dueDate AND rs.status = 1")
    List<RepaymentSchedule> findDueSchedules(LocalDate dueDate);
}

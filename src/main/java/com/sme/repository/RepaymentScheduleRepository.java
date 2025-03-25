package com.sme.repository;

import com.sme.entity.RepaymentSchedule;
import com.sme.entity.SmeLoanRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RepaymentScheduleRepository extends JpaRepository<RepaymentSchedule, Long> {
    List<RepaymentSchedule> findBySmeLoanId(Long smeLoanId);

    // Find all repayment schedules with due dates on the given date
    List<RepaymentSchedule> findByDueDate(LocalDate dueDate);

    List<RepaymentSchedule> findSchedulesByGraceEndDate(LocalDate graceEndDate);

    List<RepaymentSchedule> findOverdueSchedulesByGraceEndDate(LocalDate today);

    // Find all schedules that are due but not yet fully paid
    List<RepaymentSchedule> findByDueDateAndStatusNot(LocalDate dueDate, Integer status);

    // Fetch due schedules for today
    @Query("SELECT r FROM RepaymentSchedule r WHERE r.dueDate = :today")
    List<RepaymentSchedule> findDueSchedules(@Param("today") LocalDate today);

    // Fetch overdue schedules (past due but unpaid)
    @Query("SELECT r FROM RepaymentSchedule r WHERE r.dueDate < :today AND r.status = 0")
    List<RepaymentSchedule> findOverdueSchedules(@Param("today") LocalDate today);

    @Query("SELECT rs FROM RepaymentSchedule rs " +
            "WHERE rs.dueDate <= :today " +
            "AND (rs.status != 6) " +
            "AND (rs.dueDate = :today " +
            "OR (:today BETWEEN rs.dueDate AND rs.graceEndDate) " +
            "OR :today > rs.graceEndDate)")
    List<RepaymentSchedule> findSchedulesForProcessing(@Param("today") LocalDate today);

    @Query("SELECT rs FROM RepaymentSchedule rs WHERE rs.smeLoan.id = :loanId AND rs.interestOverDue > 0 ORDER BY rs.dueDate")
    List<RepaymentSchedule> findOverdueSchedulesByLoanOrderByDueDate(@Param("loanId") Long loanId);

    @Query("SELECT rs FROM RepaymentSchedule rs WHERE rs.smeLoan.id = :loanId ORDER BY rs.id")
    List<RepaymentSchedule> findBySmeLoanOrderById(@Param("loanId") Long loanId);

    List<RepaymentSchedule> findBySmeLoan(SmeLoanRegistration loan);

    List<RepaymentSchedule> findBySmeLoanAndStatusNot(SmeLoanRegistration loan, int status);

}

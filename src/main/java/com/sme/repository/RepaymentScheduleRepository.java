package com.sme.repository;

import com.sme.entity.RepaymentSchedule;
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

    // Find all schedules that are due but not yet fully paid
    List<RepaymentSchedule> findByDueDateAndStatusNot(LocalDate dueDate, Integer status);

    // Fetch due schedules for today
    @Query("SELECT r FROM RepaymentSchedule r WHERE r.dueDate = :today")
    List<RepaymentSchedule> findDueSchedules(@Param("today") LocalDate today);

    // Fetch overdue schedules (past due but unpaid)
    @Query("SELECT r FROM RepaymentSchedule r WHERE r.dueDate < :today AND r.status = 0")
    List<RepaymentSchedule> findOverdueSchedules(@Param("today") LocalDate today);
}

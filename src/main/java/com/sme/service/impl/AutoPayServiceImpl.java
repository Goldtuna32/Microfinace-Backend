package com.sme.service.impl;

import com.sme.entity.*;
import com.sme.repository.*;
import com.sme.service.AutoPaymentService;
import com.sme.service.HolidayService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.Set; // Add this import
import java.util.HashSet; // Add this import
import java.util.stream.Collectors;

@Service
@Transactional
public class AutoPayServiceImpl implements AutoPaymentService {

    private final HolidayService holidayService;
    private final RepaymentScheduleRepository repaymentScheduleRepository;
    private final RepaymentTransactionRepository repaymentTransactionRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final AccountTransactionRepository accountTransactionRepository;

    public AutoPayServiceImpl(HolidayService holidayService,
            RepaymentScheduleRepository repaymentScheduleRepository,
            RepaymentTransactionRepository repaymentTransactionRepository,
            CurrentAccountRepository currentAccountRepository,
            AccountTransactionRepository accountTransactionRepository) {
        this.holidayService = holidayService;
        this.repaymentScheduleRepository = repaymentScheduleRepository;
        this.repaymentTransactionRepository = repaymentTransactionRepository;
        this.currentAccountRepository = currentAccountRepository;
        this.accountTransactionRepository = accountTransactionRepository;
    }

    @Scheduled(cron = "0 * * * * *") // Runs every minute
    @Override
    public void processAutoPayments() {
        LocalDate today = LocalDate.now();

        // Testig so close this function!!!
        // if (holidayService.isHoliday(today)) {
        // System.out.println("Skipping AutoPay: Today is a holiday.");
        // return;
        // }

        System.out.println("====== Auto Pay Starting - " + today + " ======");

        // Get all schedules that are:
        // 1. Due today
        // 2. In grace period (due date passed but before grace end)
        // 3. Overdue (past grace end date)
        List<RepaymentSchedule> schedulesToProcess = repaymentScheduleRepository.findSchedulesForProcessing(today);

        // Process all schedules at once instead of individually
        boolean isOverdue = false;
        if (!schedulesToProcess.isEmpty()) {
            isOverdue = today.isAfter(schedulesToProcess.get(0).getGraceEndDate());
            processSchedules(schedulesToProcess, isOverdue);
        }
    }

    private void processSchedules(List<RepaymentSchedule> schedules, boolean isOverdue) {
        Map<Long, List<RepaymentSchedule>> schedulesByLoan = schedules.stream()
                .collect(Collectors.groupingBy(schedule -> schedule.getSmeLoan().getId()));

        for (Map.Entry<Long, List<RepaymentSchedule>> entry : schedulesByLoan.entrySet()) {
            List<RepaymentSchedule> loanSchedules = entry.getValue();
            RepaymentSchedule firstSchedule = loanSchedules.get(0);
            SmeLoanRegistration loan = firstSchedule.getSmeLoan();

            if (loan == null || loan.getCurrentAccount() == null) {
                System.out.println("Skipping: No linked loan or account");
                continue;
            }

            CurrentAccount account = loan.getCurrentAccount();
            BigDecimal balance = account.getBalance();
            BigDecimal holdAmount = account.getHoldAmount() != null ? account.getHoldAmount() : BigDecimal.ZERO;
            BigDecimal totalAvailable = balance.add(holdAmount);

            // Skip all processing if no balance
            if (totalAvailable.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Skipping all processing: No balance available");
                continue;
            }

            // Get all overdue schedules ordered by ID
            List<RepaymentSchedule> allSchedules = repaymentScheduleRepository
                    .findBySmeLoanOrderById(loan.getId());

            processPaymentsInOrder(allSchedules, account, totalAvailable, isOverdue);
        }
    }

    private void processPaymentsInOrder(List<RepaymentSchedule> schedules, CurrentAccount account,
            BigDecimal totalAvailable, boolean isOverdue) {
        BigDecimal remainingBalance = totalAvailable;
        LocalDate today = LocalDate.now();

        // Sort all schedules by due date
        schedules.sort((a, b) -> a.getDueDate().compareTo(b.getDueDate()));

        // Check for 90+ days late case first - using maximum late days
        long maxLateDays = schedules.stream()
                .filter(s -> s.getStatus() != 6)
                .mapToLong(s -> {
                    LocalDate startDate = s.getLastPaymentDate() != null ? s.getLastPaymentDate() : s.getDueDate();
                    return ChronoUnit.DAYS.between(startDate, today);
                })
                .max()
                .orElse(0);

        List<RepaymentSchedule> lateSchedules = maxLateDays >= 90 ? schedules.stream()
                .filter(s -> s.getStatus() != 6)
                .collect(Collectors.toList())
                : Collections.emptyList();

        if (!lateSchedules.isEmpty()) {
            // Calculate one common late fee for all late terms
            BigDecimal totalOutstanding = calculateTotalOutstanding(lateSchedules);
            BigDecimal totalLateFee = calculate90DaysLateFee(lateSchedules.get(0).getSmeLoan(),
                    totalOutstanding, 90);

            // Check if we have enough money for total late fee
            if (remainingBalance.compareTo(totalLateFee) >= 0) {
                // Create single transaction for all late terms
                RepaymentTransaction transaction = new RepaymentTransaction();
                transaction.setPaymentDate(Timestamp.valueOf(LocalDateTime.now()));
                transaction.setPaidLateFee(totalLateFee);
                transaction.setPaidIOD(BigDecimal.ZERO);
                transaction.setLateFeePaidDate(LocalDateTime.now());
                transaction.setPaidPrincipal(BigDecimal.ZERO);
                transaction.setPaidInterest(BigDecimal.ZERO);
                transaction.setRemainingPrincipal(lateSchedules.get(0).getRemainingPrincipal());
                transaction.setCurrentAccount(account);
                transaction.setRepaymentSchedule(lateSchedules.get(0));
                transaction.setStatus(1);
                repaymentTransactionRepository.save(transaction);

                // Update all late terms with same last payment date
                for (RepaymentSchedule lateSchedule : lateSchedules) {
                    lateSchedule.setLastPaymentDate(today);
                    repaymentScheduleRepository.save(lateSchedule);
                }

                // Update account balance
                remainingBalance = remainingBalance.subtract(totalLateFee);
                account.setBalance(remainingBalance);
            } else {
                // Not enough for full payment - hold all available money
                account.setHoldAmount(remainingBalance);
                account.setBalance(BigDecimal.ZERO);
                currentAccountRepository.save(account);
                return; // Exit processing as all money is held
            }
        }

        // Remove duplicate 90-day late case check block here

        // Continue with normal processing for non-90-day cases
        for (RepaymentSchedule schedule : schedules) {
            if (remainingBalance.compareTo(BigDecimal.ZERO) <= 0)
                break;

            BigDecimal lateFee = calculateLateFee(schedule);
            if (lateFee.compareTo(BigDecimal.ZERO) > 0) {
                if (remainingBalance.compareTo(lateFee) >= 0) {
                    createLateFeeTransaction(schedule, account, lateFee, BigDecimal.ZERO);
                    remainingBalance = remainingBalance.subtract(lateFee);

                    // Update all related schedules for 90+ days late fee
                    long lateDays = ChronoUnit.DAYS.between(schedule.getLateFeeStartDate(), today);
                    if (lateDays >= 90) {
                        List<RepaymentSchedule> relatedSchedules = schedules.stream()
                                .filter(s -> s.getId() <= schedule.getId() && s.getStatus() != 6)
                                .collect(Collectors.toList());

                        for (RepaymentSchedule relatedSchedule : relatedSchedules) {
                            relatedSchedule.setLastPaymentDate(today);
                            repaymentScheduleRepository.save(relatedSchedule);
                        }
                    } else {
                        // Normal late fee - update only current schedule
                        schedule.setLastPaymentDate(today);
                        repaymentScheduleRepository.save(schedule);
                    }
                } else {
                    // Insufficient payment - only move to hold, no transaction or date update
                    account.setHoldAmount(remainingBalance);
                    account.setBalance(BigDecimal.ZERO);
                    currentAccountRepository.save(account);
                    return;
                }
            }
        }

        // Process IODs next
        for (RepaymentSchedule schedule : schedules) {
            if (remainingBalance.compareTo(BigDecimal.ZERO) <= 0)
                break;

            BigDecimal iod = schedule.getInterestOverDue();
            if (iod.compareTo(BigDecimal.ZERO) > 0) {
                if (remainingBalance.compareTo(iod) >= 0) {
                    createLateFeeTransaction(schedule, account, BigDecimal.ZERO, iod);
                    remainingBalance = remainingBalance.subtract(iod);
                    schedule.setInterestOverDue(BigDecimal.ZERO);
                    schedule.setLastPaymentDate(LocalDate.now());

                    // Set status to 6 if IOD is cleared and no other payments pending
                    if (schedule.getInterestAmount().compareTo(BigDecimal.ZERO) == 0
                            && schedule.getPrincipalAmount().compareTo(BigDecimal.ZERO) == 0) {
                        schedule.setStatus(6);
                    }
                    repaymentScheduleRepository.save(schedule);
                } else {
                    createLateFeeTransaction(schedule, account, BigDecimal.ZERO, remainingBalance);
                    schedule.setInterestOverDue(iod.subtract(remainingBalance));
                    schedule.setLastPaymentDate(LocalDate.now());
                    remainingBalance = BigDecimal.ZERO;
                    repaymentScheduleRepository.save(schedule);
                    break;
                }
            }
        }

        // Process schedules in grace period
        if (remainingBalance.compareTo(BigDecimal.ZERO) > 0) {
            // Removed duplicate today declaration
            for (RepaymentSchedule schedule : schedules) {
                if (remainingBalance.compareTo(BigDecimal.ZERO) <= 0)
                    break;

                // Check if schedule is in grace period
                if (today.isAfter(schedule.getDueDate()) && today.isBefore(schedule.getGraceEndDate())
                        || today.isEqual(schedule.getGraceEndDate())) {
                    processIndividualSchedule(schedule, account, isOverdue);
                }
            }
        }

        // Update final account balance
        account.setBalance(remainingBalance);
        account.setHoldAmount(BigDecimal.ZERO);
        currentAccountRepository.save(account);
    }

    private void processIndividualSchedule(RepaymentSchedule schedule, CurrentAccount account, boolean isOverdue) {
        System.out.println("\n=== Processing Schedule ID: " + schedule.getId() + " ===");

        LocalDate today = LocalDate.now();
        LocalDate startDate = schedule.getLateFeeStartDate();
        LocalDate dueDate = schedule.getDueDate();
        LocalDate graceEndDate = schedule.getGraceEndDate();

        // Calculate late days using lastPaymentDate if available
        long lateDays = ChronoUnit.DAYS.between(startDate, today);

        System.out.println("Schedule " + schedule.getId() + " Late Days Calculation:");
        System.out.println("  Start Date: " + startDate);
        System.out.println("  Today: " + today);
        System.out.println("  Late Days: " + lateDays);

        // Skip if already processed successfully
        if (schedule.getStatus() == 6) {
            System.out.println("Skipping: Already completed");
            return;
        }

        // Modified check: Process if within grace period OR if overdue
        if (today.isBefore(dueDate) ||
                (!isOverdue && today.isAfter(graceEndDate))) {
            System.out.println("Schedule ID " + schedule.getId() +
                    ": Not in processing period. Due: " + dueDate +
                    ", Grace End: " + graceEndDate);
            return;
        }

        SmeLoanRegistration loan = schedule.getSmeLoan();
        if (loan == null || loan.getCurrentAccount() == null) {
            System.out.println("Skipping: No linked loan or account");
            return; // Changed from continue to return
        }

        // Remove duplicate account declaration and use the one passed as parameter
        BigDecimal balance = account.getBalance();
        BigDecimal holdAmount = account.getHoldAmount() != null ? account.getHoldAmount() : BigDecimal.ZERO;

        // Remove the duplicate late fee calculation block
        // The late fees are now handled in processLateFees method
        if (schedule.getInterestOverDue().compareTo(BigDecimal.ZERO) > 0) {
            // Skip late fee processing here as it's already handled in processLateFees
            return;
        }

        // Continue with normal processing
        System.out.println("Processing during grace period - Day: " + today);
        System.out.println("Current Balance: " + balance);
        System.out.println("Required Interest: " + schedule.getInterestAmount());

        // If it's grace end date and insufficient balance, move to IOD
        if (today.isEqual(graceEndDate) && balance.compareTo(schedule.getInterestAmount()) < 0) {
            BigDecimal currentInterest = schedule.getInterestAmount();
            BigDecimal paidInterest = balance; // Use whatever balance is available
            BigDecimal remainingInterest = currentInterest.subtract(paidInterest);

            // Create transaction for the partial payment
            RepaymentTransaction transaction = new RepaymentTransaction();
            transaction.setPaymentDate(Timestamp.valueOf(LocalDateTime.now()));
            transaction.setPaidPrincipal(BigDecimal.ZERO);
            transaction.setPaidInterest(paidInterest);
            transaction.setPaidLateFee(BigDecimal.ZERO);
            transaction.setPaidIOD(BigDecimal.ZERO);
            transaction.setRemainingPrincipal(schedule.getRemainingPrincipal());
            transaction.setCurrentAccount(account);
            transaction.setRepaymentSchedule(schedule);
            transaction.setStatus(1); // Partial payment
            repaymentTransactionRepository.save(transaction);

            // Update account balance to zero
            account.setBalance(BigDecimal.ZERO);
            currentAccountRepository.save(account);

            // Move remaining interest to IOD
            schedule.setInterestOverDue(schedule.getInterestOverDue().add(remainingInterest));
            schedule.setInterestAmount(BigDecimal.ZERO);
            repaymentScheduleRepository.save(schedule);

            System.out.println("Grace period ended - Partial payment: " + paidInterest);
            System.out.println("Moving remaining interest to IOD: " + remainingInterest);
            return; // Changed from continue to return since we're in a method
        }
        BigDecimal requiredInterest = schedule.getInterestAmount();
        BigDecimal requiredPrincipal = schedule.getPrincipalAmount();
        BigDecimal requiredLateFee = calculateLateFee(schedule);
        BigDecimal paidInterest = BigDecimal.ZERO;
        BigDecimal paidPrincipal = BigDecimal.ZERO;
        BigDecimal paidLateFee = BigDecimal.ZERO;
        BigDecimal paidIOD = BigDecimal.ZERO;

        // Deduct Late Fee First (If applicable)
        if (isOverdue && balance.compareTo(requiredLateFee) >= 0) {
            paidLateFee = requiredLateFee;
            balance = balance.subtract(paidLateFee);
        }

        // Deduct Interest First
        if (balance.compareTo(requiredInterest) >= 0) {
            paidInterest = requiredInterest;
            balance = balance.subtract(paidInterest);
            schedule.setInterestAmount(BigDecimal.ZERO); // Full interest paid
        } else {
            paidIOD = requiredInterest.subtract(balance);
            paidInterest = balance;
            balance = BigDecimal.ZERO;
            // Update remaining interest amount
            schedule.setInterestAmount(requiredInterest.subtract(paidInterest));
        }

        // Deduct Principal Next
        if (balance.compareTo(requiredPrincipal) >= 0) {
            paidPrincipal = requiredPrincipal;
            balance = balance.subtract(paidPrincipal);
        }

        // Update account balance
        account.setBalance(balance);
        currentAccountRepository.save(account);

        // Only create transaction if any payment was made (including IOD)
        if (paidPrincipal.compareTo(BigDecimal.ZERO) > 0
                || paidInterest.compareTo(BigDecimal.ZERO) > 0
                || paidLateFee.compareTo(BigDecimal.ZERO) > 0
                || paidIOD.compareTo(BigDecimal.ZERO) > 0) {

            RepaymentTransaction transaction = new RepaymentTransaction();
            transaction.setPaymentDate(Timestamp.valueOf(LocalDateTime.now()));
            transaction.setPaidPrincipal(paidPrincipal);
            transaction.setPaidInterest(paidInterest);
            transaction.setPaidLateFee(paidLateFee);
            transaction.setPaidIOD(paidIOD);
            transaction.setRemainingPrincipal(schedule.getRemainingPrincipal().subtract(paidPrincipal));
            transaction.setCurrentAccount(account);
            transaction.setRepaymentSchedule(schedule);

            // Set status to 6 only if both interest and IOD are fully paid
            boolean isFullPayment = paidPrincipal.compareTo(requiredPrincipal) == 0
                    && schedule.getInterestAmount().compareTo(BigDecimal.ZERO) == 0
                    && schedule.getInterestOverDue().compareTo(BigDecimal.ZERO) == 0;

            if (isFullPayment) {
                transaction.setStatus(6);
                schedule.setStatus(6); // Update schedule status
                repaymentScheduleRepository.save(schedule);
            } else {
                transaction.setStatus(1);
            }

            repaymentTransactionRepository.save(transaction);
        }

        // Update schedule status only if both interest and IOD are zero
        if (schedule.getInterestAmount().compareTo(BigDecimal.ZERO) == 0
                && schedule.getInterestOverDue().compareTo(BigDecimal.ZERO) == 0) {
            schedule.setStatus(6);
            repaymentScheduleRepository.save(schedule);
            System.out.println("Schedule ID " + schedule.getId() + " marked as completed after payment");
        }
    }

    private BigDecimal calculateLateFee(RepaymentSchedule schedule) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = schedule.getLateFeeStartDate();
        long lateDays = ChronoUnit.DAYS.between(startDate, today);

        if (lateDays <= 0) {
            return BigDecimal.ZERO;
        }

        SmeLoanRegistration loan = schedule.getSmeLoan();
        // Check if any schedule is 90+ days late
        List<RepaymentSchedule> allSchedules = repaymentScheduleRepository.findBySmeLoan(loan);
        boolean has90DaysLate = allSchedules.stream()
                .anyMatch(s -> ChronoUnit.DAYS.between(s.getDueDate(), today) >= 90);

        if (has90DaysLate) {
            // For 90+ days late, calculate one common late fee for all active schedules
            List<RepaymentSchedule> activeSchedules = allSchedules.stream()
                    .filter(s -> s.getStatus() != 6)
                    .collect(Collectors.toList());

            BigDecimal totalOutstanding = calculateTotalOutstanding(activeSchedules);
            return calculate90DaysLateFee(loan, totalOutstanding, 90); // Use fixed 90 days
        }

        // Regular late fee calculation for non-90-day cases
        BigDecimal interestOverDue = schedule.getInterestOverDue();
        BigDecimal annualRatePercentage = loan.getLate_fee_rate();

        if (annualRatePercentage == null) {
            annualRatePercentage = new BigDecimal("4.00"); // 4% default annual rate
        }

        BigDecimal dailyRate = annualRatePercentage
                .divide(new BigDecimal("100"))
                .divide(new BigDecimal("365"), 10, BigDecimal.ROUND_HALF_UP);

        BigDecimal lateFee = interestOverDue.multiply(dailyRate).multiply(BigDecimal.valueOf(lateDays));
        return lateFee.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private void processAvailableLateFees(List<RepaymentSchedule> overdueSchedules,
            CurrentAccount account, BigDecimal totalAvailable, Map<Long, BigDecimal> lateFeesBySchedule) {
        BigDecimal balance = totalAvailable;
        account.setHoldAmount(BigDecimal.ZERO);

        for (RepaymentSchedule overdueSchedule : overdueSchedules) {
            BigDecimal lateFee = lateFeesBySchedule.get(overdueSchedule.getId());
            BigDecimal requiredIOD = overdueSchedule.getInterestOverDue();
            BigDecimal paidIOD = BigDecimal.ZERO;

            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                if (balance.compareTo(requiredIOD) >= 0) {
                    paidIOD = requiredIOD;
                    balance = balance.subtract(paidIOD);
                    overdueSchedule.setInterestOverDue(BigDecimal.ZERO);
                } else {
                    paidIOD = balance;
                    balance = BigDecimal.ZERO;
                    overdueSchedule.setInterestOverDue(requiredIOD.subtract(paidIOD));
                }

                if (lateFee.compareTo(BigDecimal.ZERO) > 0 || paidIOD.compareTo(BigDecimal.ZERO) > 0) {
                    createLateFeeTransaction(overdueSchedule, account, lateFee, paidIOD);
                }

                repaymentScheduleRepository.save(overdueSchedule);
            }
        }

        account.setBalance(balance);
    }

    private void createLateFeeTransaction(RepaymentSchedule schedule, CurrentAccount account,
            BigDecimal lateFee, BigDecimal paidIOD) {
        // Skip creating transaction if both lateFee and paidIOD are zero
        if (lateFee.compareTo(BigDecimal.ZERO) <= 0 && paidIOD.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // Only update lastPaymentDate if there's an actual payment
        if (lateFee.compareTo(BigDecimal.ZERO) > 0 || paidIOD.compareTo(BigDecimal.ZERO) > 0) {
            schedule.setLastPaymentDate(LocalDate.now());
            repaymentScheduleRepository.save(schedule);
        }

        RepaymentTransaction transaction = new RepaymentTransaction();
        transaction.setPaymentDate(Timestamp.valueOf(LocalDateTime.now()));
        transaction.setPaidLateFee(lateFee);
        transaction.setPaidIOD(paidIOD);
        transaction.setLateFeePaidDate(LocalDateTime.now());
        transaction.setPaidPrincipal(BigDecimal.ZERO);
        transaction.setPaidInterest(BigDecimal.ZERO);
        transaction.setRemainingPrincipal(schedule.getRemainingPrincipal());
        transaction.setCurrentAccount(account);
        transaction.setRepaymentSchedule(schedule);
        transaction.setStatus(1);
        repaymentTransactionRepository.save(transaction);
    }

    // Add these two helper methods
    private BigDecimal calculateTotalOutstanding(List<RepaymentSchedule> schedules) {
        BigDecimal totalOutstanding = BigDecimal.ZERO;
        for (RepaymentSchedule schedule : schedules) {
            totalOutstanding = totalOutstanding
                    .add(schedule.getInterestOverDue())
                    .add(schedule.getInterestAmount())
                    .add(schedule.getPrincipalAmount());
        }
        return totalOutstanding;
    }

    private BigDecimal calculate90DaysLateFee(SmeLoanRegistration loan, BigDecimal totalOutstanding, long lateDays) {
        BigDecimal ninetyDayRate = loan.getNinety_day_late_fee_rate();
        if (ninetyDayRate == null) {
            ninetyDayRate = new BigDecimal("8.00"); // 8% default rate for 90+ days
        }

        BigDecimal dailyRate = ninetyDayRate
                .divide(new BigDecimal("100"))
                .divide(new BigDecimal("365"), 10, BigDecimal.ROUND_HALF_UP);

        BigDecimal lateFee = totalOutstanding.multiply(dailyRate).multiply(BigDecimal.valueOf(lateDays));
        return lateFee.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
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
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;    // Add this import
import java.util.HashSet;  // Add this import
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

        if (holidayService.isHoliday(today)) {
            System.out.println("Skipping AutoPay: Today is a holiday.");
            return;
        }

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

        Set<Long> processedLoans = new HashSet<>();

        for (Map.Entry<Long, List<RepaymentSchedule>> entry : schedulesByLoan.entrySet()) {
            Long loanId = entry.getKey();
            if (processedLoans.contains(loanId)) {
                continue;
            }
            
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
            
            // Process late fees once per loan
            if (loanSchedules.stream().anyMatch(s -> s.getInterestOverDue().compareTo(BigDecimal.ZERO) > 0)) {
                List<RepaymentSchedule> overdueSchedules = repaymentScheduleRepository
                    .findOverdueSchedulesByLoanOrderByDueDate(loan.getId());
                
                processLateFees(overdueSchedules, account, balance, holdAmount);
            }

            // Continue processing individual schedules
            for (RepaymentSchedule schedule : loanSchedules) {
                processIndividualSchedule(schedule, account, isOverdue);
            }

            processedLoans.add(loanId);
        }
    }

    private void processLateFees(List<RepaymentSchedule> overdueSchedules, CurrentAccount account, 
            BigDecimal balance, BigDecimal holdAmount) {
        BigDecimal totalAvailable = balance.add(holdAmount);
        BigDecimal totalLateFee = BigDecimal.ZERO;
        
        // Calculate total late fees once
        Map<Long, BigDecimal> lateFeesBySchedule = new HashMap<>();
        for (RepaymentSchedule overdueSchedule : overdueSchedules) {
            BigDecimal lateFee = calculateLateFee(overdueSchedule);
            lateFeesBySchedule.put(overdueSchedule.getId(), lateFee);
            totalLateFee = totalLateFee.add(lateFee);
            
            System.out.println("Term " + overdueSchedule.getId() + " Late Fee: " + lateFee);
        }
        
        System.out.println("Total Late Fee Required: " + totalLateFee);
        System.out.println("Total Available: " + totalAvailable);

        // Process late fees
        if (totalAvailable.compareTo(totalLateFee) >= 0) {
            processAvailableLateFees(overdueSchedules, account, totalAvailable, lateFeesBySchedule);
        } else {
            account.setHoldAmount(totalAvailable);
            account.setBalance(BigDecimal.ZERO);
            System.out.println("Holding amount: " + totalAvailable + " for late fees");
        }
        
        currentAccountRepository.save(account);
    }

    private void processIndividualSchedule(RepaymentSchedule schedule, CurrentAccount account, boolean isOverdue) {
        System.out.println("\n=== Processing Schedule ID: " + schedule.getId() + " ===");

        LocalDate today = LocalDate.now();
        LocalDate dueDate = schedule.getDueDate();
        LocalDate graceEndDate = schedule.getGraceEndDate();

        // Calculate late days for logging
        Optional<RepaymentTransaction> lastLateFeePayment = repaymentTransactionRepository
            .findTopByRepaymentScheduleAndLateFeePaidDateIsNotNullOrderByLateFeePaidDateDesc(schedule);
        LocalDate startDate = lastLateFeePayment
            .map(transaction -> transaction.getLateFeePaidDate().toLocalDate())
            .orElse(dueDate);
        long lateDays = ChronoUnit.DAYS.between(startDate, today);

        System.out.println("Schedule " + schedule.getId() + " Late Days Calculation:");
        System.out.println("  Start Date: " + startDate);
        System.out.println("  Today: " + today);
        System.out.println("  Late Days: " + lateDays);

        // Skip if already processed successfully
        if (schedule.getStatus() == 6) {
            System.out.println("Skipping: Already completed");
            return; // Changed from continue to return
        }

        // Modified check: Process if within grace period OR if overdue
        if (today.isBefore(dueDate) || 
            (!isOverdue && today.isAfter(graceEndDate))) {
            System.out.println("Schedule ID " + schedule.getId() + 
                ": Not in processing period. Due: " + dueDate + 
                ", Grace End: " + graceEndDate);
            return; // Changed from continue to return
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
        LocalDate dueDate = schedule.getDueDate();

        Optional<RepaymentTransaction> lastLateFeePayment = repaymentTransactionRepository
            .findTopByRepaymentScheduleAndLateFeePaidDateIsNotNullOrderByLateFeePaidDateDesc(schedule);

        LocalDate startDate = lastLateFeePayment
            .map(transaction -> transaction.getLateFeePaidDate().toLocalDate())
            .orElse(dueDate);

        long lateDays = ChronoUnit.DAYS.between(startDate, today);
        
        

        if (lateDays > 0) {
            BigDecimal interestOverDue = schedule.getInterestOverDue();
            BigDecimal annualRatePercentage = schedule.getSmeLoan().getLate_fee_rate();
            
            if (annualRatePercentage == null) {
                annualRatePercentage = new BigDecimal("4.00"); // 4% default annual rate
            }

            // Convert annual rate to daily rate
            BigDecimal dailyRate = annualRatePercentage
                .divide(new BigDecimal("100")) // Convert percentage to decimal
                .divide(new BigDecimal("365"), 10, BigDecimal.ROUND_HALF_UP); // Convert to daily rate

            BigDecimal lateFee = interestOverDue.multiply(dailyRate).multiply(BigDecimal.valueOf(lateDays));
            return lateFee.setScale(2, BigDecimal.ROUND_HALF_UP); // Round to 2 decimal places
        }

        return BigDecimal.ZERO;
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
}
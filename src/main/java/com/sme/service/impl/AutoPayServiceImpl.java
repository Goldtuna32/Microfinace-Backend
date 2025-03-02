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
import java.util.List;
import java.util.Optional;

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

        // Skip processing if today is a holiday
        if (holidayService.isHoliday(today)) {
            System.out.println("Skipping AutoPay: Today is a holiday.");
            return;
        }

        System.out.println(
                "--------------------------------------Auto Pay is Activating...--------------------------------------");
        System.out.println(
                "--------------------------------------Auto Pay is Activating...--------------------------------------");
        System.out.println(
                "--------------------------------------Auto Pay is Activating...--------------------------------------");
        System.out.println(
                "--------------------------------------Auto Pay is Activating...--------------------------------------");
        System.out.println(
                "--------------------------------------Auto Pay is Activating...--------------------------------------");

        // Step 1: Process Due Schedules for Today
        List<RepaymentSchedule> dueSchedules = repaymentScheduleRepository.findDueSchedules(today);
        processSchedules(dueSchedules, false);

        // Step 2: Process Overdue Schedules for Late Fees
        List<RepaymentSchedule> overdueSchedules = repaymentScheduleRepository.findOverdueSchedules(today);
        processSchedules(overdueSchedules, true);
    }

    private void processSchedules(List<RepaymentSchedule> schedules, boolean isOverdue) {
        for (RepaymentSchedule schedule : schedules) {
            // Skip if already processed successfully
            if (schedule.getStatus() == 6) {
                System.out.println("Skipping schedule ID " + schedule.getId() + ": Already processed successfully.");
                continue;
            }

            SmeLoanRegistration loan = schedule.getSmeLoan();

            if (loan == null || loan.getCurrentAccount() == null) {
                System.out.println("Skipping schedule ID " + schedule.getId() + ": No linked loan or account.");
                continue;
            }

            CurrentAccount account = loan.getCurrentAccount();
            BigDecimal balance = account.getBalance();
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
            } else {
                paidIOD = requiredInterest.subtract(balance);
                paidInterest = balance;
                balance = BigDecimal.ZERO;
            }

            // Deduct Principal Next
            if (balance.compareTo(requiredPrincipal) >= 0) {
                paidPrincipal = requiredPrincipal;
                balance = balance.subtract(paidPrincipal);
            }

            // Update account balance
            account.setBalance(balance);
            currentAccountRepository.save(account);

            // Log Transaction
            RepaymentTransaction transaction = new RepaymentTransaction();
            transaction.setPaymentDate(Timestamp.valueOf(LocalDateTime.now()));
            transaction.setPaidPrincipal(paidPrincipal);
            transaction.setPaidInterest(paidInterest);
            transaction.setPaidLateFee(paidLateFee);
            transaction.setPaidIOD(paidIOD);
            transaction.setRemainingPrincipal(schedule.getRemainingPrincipal().subtract(paidPrincipal));
            transaction.setCurrentAccount(account);
            transaction.setRepaymentSchedule(schedule);
            
            // Set status to 6 for successful transaction
            boolean isFullPayment = paidPrincipal.compareTo(requiredPrincipal) == 0 
                                  && paidInterest.compareTo(requiredInterest) == 0;
            if (isFullPayment) {
                transaction.setStatus(6);
                schedule.setStatus(6);  // Update schedule status
                repaymentScheduleRepository.save(schedule);
            } else {
                transaction.setStatus(1);
            }
            
            repaymentTransactionRepository.save(transaction);
            System.out.println("Processed payment for schedule ID " + schedule.getId() + 
                             " with status: " + (isFullPayment ? "SUCCESS(6)" : "PARTIAL(1)"));
        }
    }

    private BigDecimal calculateLateFee(RepaymentSchedule schedule) {
        LocalDate dueDate = schedule.getDueDate();
        LocalDate graceEndDate = schedule.getGraceEndDate();
        LocalDate today = LocalDate.now();

        // If still within grace period, no late fee
        if (today.isBefore(graceEndDate) || today.isEqual(graceEndDate)) {
            return BigDecimal.ZERO;
        }

        // Fetch last transaction date
        CurrentAccount account = schedule.getSmeLoan().getCurrentAccount();
        Optional<AccountTransaction> lastTransaction = accountTransactionRepository
                .findLatestTransactionByAccount(account.getId());

        if (lastTransaction.isPresent()) {
            // Convert java.util.Date to LocalDate
            LocalDate transactionDate = lastTransaction.get().getTransactionDate().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
            long lateDays = ChronoUnit.DAYS.between(graceEndDate.plusDays(1), transactionDate);

            if (lateDays > 0) {
                BigDecimal interestDue = schedule.getInterestAmount();
                BigDecimal rate = new BigDecimal("0.05"); // Example: 5% daily late fee
                return interestDue.multiply(rate).multiply(BigDecimal.valueOf(lateDays));
            }
        }
        return BigDecimal.ZERO;
    }
}

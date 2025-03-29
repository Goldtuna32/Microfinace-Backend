package com.sme.service.impl;

import com.sme.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sme.entity.*;
import com.sme.repository.*;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class LoanAutoPayment implements AutoPaymentStrategy {
    private final HolidayService holidayService;
    private final RepaymentScheduleRepository repaymentScheduleRepository;
    private final RepaymentTransactionRepository repaymentTransactionRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final AccountTransactionRepository accountTransactionRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final NotificationService notificationService;

    private final Map<Long, LocalDate> lastNotified = new ConcurrentHashMap<>();

    public LoanAutoPayment(HolidayService holidayService,
                           RepaymentScheduleRepository repaymentScheduleRepository,
                           RepaymentTransactionRepository repaymentTransactionRepository,
                           CurrentAccountRepository currentAccountRepository,
                           AccountTransactionRepository accountTransactionRepository, EmailService emailService, SmsService smsService, NotificationService notificationService) {
        this.holidayService = holidayService;
        this.repaymentScheduleRepository = repaymentScheduleRepository;
        this.repaymentTransactionRepository = repaymentTransactionRepository;
        this.currentAccountRepository = currentAccountRepository;
        this.accountTransactionRepository = accountTransactionRepository;
        this.emailService = emailService;
        this.smsService = smsService;
        this.notificationService = notificationService;
    }

    @Override
    public void processPayments() {
        LocalDate today = LocalDate.now();

        // Testig so close this function!!!
        // if (holidayService.isHoliday(today)) {
        // System.out.println("Skipping AutoPay: Today is a holiday.");
        // return;
        // }

        System.out.println("====== SME Auto Pay Starting - " + today + " ======");

        // Get all schedules that are:
        // 1. Due today
        // 2. In grace period (due date passed but before grace end)
        // 3. Overdue (past grace end date)
        List<RepaymentSchedule> schedulesToProcess = repaymentScheduleRepository.findSchedulesForProcessing(today);

        // Process all schedules at once instead of individually
        
        if (!schedulesToProcess.isEmpty()) {

//            for (RepaymentSchedule schedule : schedulesToProcess) {
//                if (today.isAfter(schedule.getGraceEndDate())) { // Notify only if past grace period
//                    notifyOverduePayment(schedule);
//                }
//            }
            boolean isOverdue = schedulesToProcess.stream()
                    .anyMatch(schedule -> today.isAfter(schedule.getDueDate()));
            processSchedules(schedulesToProcess, isOverdue);
        }
    }

//    private void notifyOverduePayment(RepaymentSchedule schedule) {
//        LocalDate today = LocalDate.now();
//        LocalDate lastNotificationDate = lastNotified.get(schedule.getId());
//
//        // Skip if already notified today
//        if (lastNotificationDate != null && lastNotificationDate.equals(today)) {
//            System.out.println("Skipping notification for schedule #" + schedule.getId() +
//                    " - Already notified today.");
//            return;
//        }
//
//        SmeLoanRegistration loan = schedule.getSmeLoan();
//        CurrentAccount currentAccount = loan.getCurrentAccount();
//        CIF cif = currentAccount.getCif();
//
//        String email = cif.getEmail();
//        String rawPhoneNumber = cif.getPhoneNumber(); // e.g., "09458345022"
//        String phoneNumber = "+95" + rawPhoneNumber.replaceFirst("^0", ""); // Becomes "+959458345022"
//        String customerName = cif.getName();
//
//        String subject = "Overdue Payment Notification - Loan #" + loan.getId();
//        String emailBody = String.format(
//                "Dear %s,\n\nYour loan payment (Schedule #%d) is overdue as of %s.\n" +
//                        "Due Date: %s\nAmount: %s\nPlease make the payment at your earliest convenience.\n\n" +
//                        "Regards,\nSME Loan Team",
//                customerName, schedule.getId(), LocalDate.now(), schedule.getDueDate(),
//                schedule.getInterestAmount() != null ? schedule.getInterestAmount() : "N/A"
//        );
//
//        String smsBody = String.format(
//                "Dear %s, Your loan payment (Schedule #%d) is overdue. " +
//                        "Amount: %s. Due: %s. Please pay ASAP.",
//                customerName, schedule.getId(),
//                schedule.getInterestAmount() != null ? schedule.getInterestAmount() : "N/A",
//                schedule.getDueDate()
//        );
//
//        String notificationBody = String.format(
//                "Loan #%d payment overdue. Amount: %s. Due: %s",
//                loan.getId(),
//                schedule.getInterestAmount() != null ? schedule.getInterestAmount() : "N/A",
//                schedule.getDueDate()
//        );
//
//        try {
//            System.out.println("Attempting to send email to " + email + " with body: " + emailBody);
//            emailService.sendEmail(email, subject, emailBody);
//            System.out.println("Attempting to send SMS to " + phoneNumber + " with body: " + smsBody);
//            smsService.sendSms(phoneNumber, smsBody);
//            System.out.println("Attempting to save notification for account #" + currentAccount.getId());
//            notificationService.sendSystemNotification(
//                    currentAccount.getId(),
//                    "OVERDUE_PAYMENT",
//                    notificationBody,
//                    loan.getId()
//            );
//            System.out.println("Notifications sent for schedule #" + schedule.getId());
//            lastNotified.put(schedule.getId(), today); // Update last notified date
//        } catch (Exception e) {
//            System.err.println("Failed to send notifications for schedule #" +
//                    schedule.getId() + ": " + e.getMessage());
//        }
//    }

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

            // Get all overdue schedules ordered by ID first
            List<RepaymentSchedule> allSchedules = repaymentScheduleRepository
                    .findBySmeLoanOrderById(loan.getId());
            LocalDate today = LocalDate.now();

            // Handle grace end date cases even when no balance available
            if (totalAvailable.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("No balance available - Checking for grace end date cases");

                // Check if any schedule needs interest to IOD movement
                for (RepaymentSchedule schedule : allSchedules) {
                    if (today.isEqual(schedule.getGraceEndDate())
                            && schedule.getInterestAmount().compareTo(BigDecimal.ZERO) > 0) {
                        // Move interest to IOD only if there's actual interest to move
                        BigDecimal interest = schedule.getInterestAmount();
                        schedule.setInterestOverDue(schedule.getInterestOverDue().add(interest));
                        schedule.setInterestAmount(BigDecimal.ZERO);
                        repaymentScheduleRepository.save(schedule);
                        System.out.println("Moved interest " + interest + " to IOD for schedule " + schedule.getId());
                    }
                }
                continue;
            }
            List<RepaymentSchedule> activeSchedules = allSchedules.stream()
                    .filter(s -> s.getStatus() != 6)
                    .collect(Collectors.toList());

            processPaymentsInOrder(activeSchedules, account, totalAvailable, isOverdue );
        }
    }

    private void processPaymentsInOrder(List<RepaymentSchedule> schedules, CurrentAccount account,
            BigDecimal totalAvailable, boolean isOverdue ) {
        BigDecimal remainingBalance = totalAvailable;
        LocalDate today = LocalDate.now();

        // Sort all schedules by due date
        schedules.sort((a, b) -> a.getDueDate().compareTo(b.getDueDate()));

        // Check for 180+ or 90+ days late case first - using maximum late days
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
            BigDecimal totalLateFee;
            
            // Use different rate based on late days
            if (maxLateDays >= 180) {
                totalLateFee = calculate180DaysLateFee(lateSchedules.get(0).getSmeLoan(),
                        totalOutstanding, maxLateDays);
                System.out.println("Calculating 180+ days late fee");
            } else {
                totalLateFee = calculate90DaysLateFee(lateSchedules.get(0).getSmeLoan(),
                        totalOutstanding, maxLateDays);
                System.out.println("Calculating 90+ days late fee");
            }

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
                System.out.println("90 day late fee ");

                // Update only schedules that have IOD and are being paid
                for (RepaymentSchedule lateSchedule : lateSchedules) {
                    // Only update last payment date if schedule has IOD
                    if (lateSchedule.getInterestOverDue().compareTo(BigDecimal.ZERO) > 0) {
                        lateSchedule.setLastPaymentDate(today);

                    }
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

        // Process schedules in order
        // In processPaymentsInOrder method, update the method call:
        for (RepaymentSchedule schedule : schedules) {
        if (remainingBalance.compareTo(BigDecimal.ZERO) <= 0)
            break;
        
        // Pass schedules list to the method
        processIndividualSchedule(schedule, account, isOverdue, schedules , remainingBalance);
        remainingBalance = account.getBalance();
    }
        }
        
        // Update the method signature and implementation:
        private void processIndividualSchedule(RepaymentSchedule schedule, CurrentAccount account, 
        boolean isOverdue, List<RepaymentSchedule> schedules , BigDecimal remainingBalance) {
        System.out.println("\n=== Processing Schedule ID: " + schedule.getId() + " ===");
        System.out.println("Initial account balance: " + account.getBalance());

        LocalDate today = LocalDate.now();
        LocalDate startDate = schedule.getLateFeeStartDate();
        LocalDate dueDate = schedule.getDueDate();
        LocalDate graceEndDate = schedule.getGraceEndDate();

        // Declare required payment variables
        BigDecimal requiredLateFee = calculateLateFee(schedule);
        BigDecimal requiredInterest = schedule.getInterestAmount();
        BigDecimal requiredPrincipal = schedule.getPrincipalAmount();

        // Declare payment tracking variables
        BigDecimal paidLateFee = BigDecimal.ZERO;
        BigDecimal paidIOD = BigDecimal.ZERO;
        BigDecimal paidInterest = BigDecimal.ZERO;
        BigDecimal paidPrincipal = BigDecimal.ZERO;

        System.out.println("IS OVERDUE CHECK: " + isOverdue);

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

        // Remove duplicate account declaration and use the totalAvailable passed
        BigDecimal balance = remainingBalance;

        // Calculate total late fees first
        BigDecimal totalLateFees = BigDecimal.ZERO;
        if (isOverdue) {
            for (RepaymentSchedule lateSchedule : schedules) {
                if (lateSchedule.getStatus() != 6) {
                    totalLateFees = totalLateFees.add(calculateLateFee(lateSchedule));
                }
            }
        }

        // If not enough balance for total late fees, hold money
        if (isOverdue && totalLateFees.compareTo(BigDecimal.ZERO) > 0) {
            if (balance.compareTo(totalLateFees) < 0) {
                // Get existing hold amount and add new hold
                BigDecimal existingHold = account.getHoldAmount() != null ? account.getHoldAmount() : BigDecimal.ZERO;
                BigDecimal newTotalHold = existingHold.add(account.getBalance());
                account.setHoldAmount(newTotalHold);
                account.setBalance(BigDecimal.ZERO);
                currentAccountRepository.save(account);
                return;
            }

            // Process all late fees first
            for (RepaymentSchedule lateSchedule : schedules) {
                if (lateSchedule.getStatus() != 6) {
                    BigDecimal currentLateFee = calculateLateFee(lateSchedule);
                    if (currentLateFee.compareTo(BigDecimal.ZERO) > 0) {
                        balance = balance.subtract(currentLateFee);
                        lateSchedule.setLastPaymentDate(today);
                        repaymentScheduleRepository.save(lateSchedule);
                        
                        // Create transaction for late fee
                        RepaymentTransaction lateFeeTransaction = new RepaymentTransaction();
                        lateFeeTransaction.setPaymentDate(Timestamp.valueOf(LocalDateTime.now()));
                        lateFeeTransaction.setPaidLateFee(currentLateFee);
                        lateFeeTransaction.setPaidIOD(BigDecimal.ZERO);
                        lateFeeTransaction.setPaidInterest(BigDecimal.ZERO);
                        lateFeeTransaction.setPaidPrincipal(BigDecimal.ZERO);
                        lateFeeTransaction.setRemainingPrincipal(lateSchedule.getRemainingPrincipal());
                        lateFeeTransaction.setCurrentAccount(account);
                        lateFeeTransaction.setRepaymentSchedule(lateSchedule);
                        lateFeeTransaction.setStatus(1);
                        repaymentTransactionRepository.save(lateFeeTransaction);
                    }
                }
            }
            // Clear hold amount since we've used it for late fee payment
            account.setHoldAmount(BigDecimal.ZERO);
            account.setBalance(balance);
            currentAccountRepository.save(account);
             // here to set hold amount to zero
        }

        // Continue with regular interest and principal processing for current schedule
        BigDecimal iod = schedule.getInterestOverDue();
        if (iod.compareTo(BigDecimal.ZERO) > 0 && balance.compareTo(BigDecimal.ZERO) > 0) {
            if (balance.compareTo(iod) >= 0) {
                paidIOD = iod;
                balance = balance.subtract(iod);
                schedule.setInterestOverDue(BigDecimal.ZERO);
            } else {
                paidIOD = balance;
                schedule.setInterestOverDue(iod.subtract(balance));
                balance = BigDecimal.ZERO;
            }
            System.out.println("Payment of IOD: " + paidIOD);
        }

        // 3. Interest
        if (balance.compareTo(BigDecimal.ZERO) > 0) {
            if (balance.compareTo(requiredInterest) >= 0) {
                // Full payment
                paidInterest = requiredInterest;
                balance = balance.subtract(paidInterest);
                schedule.setInterestAmount(BigDecimal.ZERO);
            } else if (today.isEqual(schedule.getGraceEndDate())) {
                // Partial payment at grace end date
                paidInterest = balance;
                BigDecimal remainingInterest = requiredInterest.subtract(balance);
                // Move remaining interest to IOD
                schedule.setInterestOverDue(schedule.getInterestOverDue().add(remainingInterest));
                schedule.setInterestAmount(BigDecimal.ZERO);
                balance = BigDecimal.ZERO;
            } else {
                // Normal partial payment before grace end
                paidInterest = balance;
                schedule.setInterestAmount(requiredInterest.subtract(balance));
                balance = BigDecimal.ZERO;
            }
            System.out.println("Partial payment of interest: " + paidInterest);
        }

        // 4. Principal
        if (balance.compareTo(BigDecimal.ZERO) > 0) {
            if (balance.compareTo(requiredPrincipal) >= 0) {
                // Full payment
                paidPrincipal = requiredPrincipal;
                balance = balance.subtract(paidPrincipal);
                schedule.setPrincipalAmount(BigDecimal.ZERO);
                schedule.setRemainingPrincipal(BigDecimal.ZERO);
            } else {
                // Partial payment - use all remaining balance
                paidPrincipal = balance;
                BigDecimal remainingAmount = requiredPrincipal.subtract(paidPrincipal);
                schedule.setPrincipalAmount(remainingAmount);
                schedule.setRemainingPrincipal(remainingAmount);
                
                // Update all future schedules with the new remaining amount
                List<RepaymentSchedule> futureSchedules = schedules.stream()
                    .filter(s -> s.getId() > schedule.getId() && s.getStatus() != 6)
                    .collect(Collectors.toList());
                
                for (RepaymentSchedule futureSchedule : futureSchedules) {
                    futureSchedule.setPrincipalAmount(remainingAmount);
                    futureSchedule.setRemainingPrincipal(remainingAmount);
                    repaymentScheduleRepository.save(futureSchedule);
                }
                
                balance = BigDecimal.ZERO;  // Use all remaining balance for partial payment
            }
            repaymentScheduleRepository.save(schedule);
        }

        // Update account balance and save immediately
        account.setBalance(balance);
        System.out.println("Current account balance after processing: " + balance);
        CurrentAccount savedAccount = currentAccountRepository.save(account);
        currentAccountRepository.flush(); // Force immediate flush to database
        System.out.println("Saved account balance in database: " + savedAccount.getBalance());

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
        BigDecimal interestOverDue = schedule.getInterestOverDue();
        BigDecimal ratePercentage = loan.getLate_fee_rate();
        
        if (ratePercentage == null) {
            ratePercentage = new BigDecimal("3.00"); // 3% default rate
        }

        // Calculate daily rate (annual rate / 365)
        BigDecimal dailyRate = ratePercentage
                .divide(new BigDecimal("100")) // Convert percentage to decimal
                .divide(new BigDecimal("365"), 10, BigDecimal.ROUND_HALF_UP); // Get daily rate

        System.out.println("Late days: " + lateDays);
        System.out.println("IOD amount: " + interestOverDue);
        System.out.println("Rate (%): " + ratePercentage);
        System.out.println("Daily Rate: " + dailyRate);

        BigDecimal lateFee = interestOverDue.multiply(dailyRate).multiply(BigDecimal.valueOf(lateDays));
        return lateFee.setScale(2, BigDecimal.ROUND_HALF_UP);
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
    private BigDecimal calculate180DaysLateFee(SmeLoanRegistration loan, BigDecimal totalOutstanding, long lateDays) {
        BigDecimal oneEightyDayRate = loan.getOne_hundred_and_eighty_late_fee_rate();
        if (oneEightyDayRate == null) {
            oneEightyDayRate = new BigDecimal("12.00"); // 12% default rate for 180+ days
        }

        BigDecimal dailyRate = oneEightyDayRate
                .divide(new BigDecimal("100"))
                .divide(new BigDecimal("365"), 10, BigDecimal.ROUND_HALF_UP);

        BigDecimal lateFee = totalOutstanding.multiply(dailyRate).multiply(BigDecimal.valueOf(lateDays));
        return lateFee.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}

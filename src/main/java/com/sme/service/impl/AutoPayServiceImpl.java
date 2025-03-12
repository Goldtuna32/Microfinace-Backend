package com.sme.service.impl;

import com.sme.entity.*;
import com.sme.repository.*;
import com.sme.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AutoPayServiceImpl implements AutoPaymentService {

    private final HolidayService holidayService;
    private final RepaymentScheduleRepository repaymentScheduleRepository;
    private final RepaymentTransactionRepository repaymentTransactionRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final AccountTransactionRepository accountTransactionRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailService emailService;
    @Autowired
    private SmsService smsService;
    @Autowired
    private NotificationService notificationService;



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

        for (RepaymentSchedule schedule : schedulesToProcess) {
            boolean isOverdue = today.isAfter(schedule.getGraceEndDate());
//            notifyOverduePayment(schedule);
            processSchedules(Arrays.asList(schedule), isOverdue);
        }
    }

//    @Scheduled(cron = "0 0 0,6,12,18 * * *") // Runs at 00:00, 06:00, 12:00, 18:00
//    public void sendOverdueNotifications() {
//        LocalDate today = LocalDate.now();
//        if (holidayService.isHoliday(today)) {
//            System.out.println("Skipping Notifications: Today is a holiday.");
//            return;
//        }
//        System.out.println("====== Sending Overdue Notifications - " + today + " ======");
//
//        List<RepaymentSchedule> overdueSchedules = repaymentScheduleRepository
//                .findSchedulesForProcessing(today)
//                .stream()
//                .filter(schedule -> today.isAfter(schedule.getGraceEndDate()))
//                .filter(schedule -> "PENDING".equals(schedule.getStatus()))
//                .collect(Collectors.toList());
//
//        if (overdueSchedules.isEmpty()) {
//            System.out.println("No overdue PENDING schedules found for notification on " + today);
//        } else {
//            System.out.println("Found " + overdueSchedules.size() + " overdue schedules to notify.");
//            for (RepaymentSchedule schedule : overdueSchedules) {
//                notifyOverduePayment(schedule);
//            }
//        }
//    }
//
//    private void notifyOverduePayment(RepaymentSchedule schedule) {
//        LocalDate today = LocalDate.now();
//
//        // Check if a notification has already been sent today for this schedule
//        boolean alreadyNotified = notificationRepository
//                .existsBySmeLoanAndCreatedAtBetween(schedule.getSmeLoan(), today.atStartOfDay(), today.plusDays(1).atStartOfDay());
//
//        if (alreadyNotified) {
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
//        String rawPhoneNumber = cif.getPhoneNumber();
//        String phoneNumber = "+95" + rawPhoneNumber.replaceFirst("^0", "");
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
//            System.out.println("Sending email to " + email);
//            emailService.sendEmail(email, subject, emailBody);
//            System.out.println("Sending SMS to " + phoneNumber);
//            smsService.sendSms(phoneNumber, smsBody);
//
//            // Store notification in the database
//            Notification notification = new Notification();
//            notification.setAccountId(currentAccount.getId());
//            notification.setType("OVERDUE_PAYMENT");
//            notification.setMessage(notificationBody);
//            notification.setSmeLoan(loan);
//            notificationRepository.save(notification);
//
//            System.out.println("Notification saved for schedule #" + schedule.getId());
//        } catch (Exception e) {
//            System.err.println("Failed to send notifications for schedule #" + schedule.getId() + ": " + e.getMessage());
//        }
//    }


    private void processSchedules(List<RepaymentSchedule> schedules, boolean isOverdue) {
        for (RepaymentSchedule schedule : schedules) {
            System.out.println("\n=== Processing Schedule ID: " + schedule.getId() + " ===");

            LocalDate today = LocalDate.now();
            LocalDate dueDate = schedule.getDueDate();
            LocalDate graceEndDate = schedule.getGraceEndDate();

            // Skip if already processed successfully
            if (schedule.getStatus() == 6) {
                System.out.println("Skipping: Already completed");
                continue;
            }

            // Modified check: Process if within grace period OR if overdue
            if (today.isBefore(dueDate) || 
                (!isOverdue && today.isAfter(graceEndDate))) {
                System.out.println("Schedule ID " + schedule.getId() + 
                    ": Not in processing period. Due: " + dueDate + 
                    ", Grace End: " + graceEndDate);
                continue;
            }

            SmeLoanRegistration loan = schedule.getSmeLoan();
            if (loan == null || loan.getCurrentAccount() == null) {
                System.out.println("Skipping: No linked loan or account");
                continue;
            }

            CurrentAccount account = loan.getCurrentAccount();
            BigDecimal balance = account.getBalance();
            BigDecimal holdAmount = account.getHoldAmount() != null ? account.getHoldAmount() : BigDecimal.ZERO;
            
            // Get all overdue schedules for this loan ordered by due date
            if (schedule.getInterestOverDue().compareTo(BigDecimal.ZERO) > 0) {
                List<RepaymentSchedule> overdueSchedules = repaymentScheduleRepository
                    .findOverdueSchedulesByLoanOrderByDueDate(schedule.getSmeLoan().getId());
                
                BigDecimal totalAvailable = balance.add(holdAmount);
                BigDecimal totalLateFee = BigDecimal.ZERO;
                
                // Calculate total late fees for all overdue terms
                for (RepaymentSchedule overdueSchedule : overdueSchedules) {
                    BigDecimal lateFee = calculateLateFee(overdueSchedule);
                    totalLateFee = totalLateFee.add(lateFee);
                    
                    System.out.println("Term " + overdueSchedule.getId() + " Late Fee: " + lateFee);
                }
                
                System.out.println("Total Late Fee Required: " + totalLateFee);
                System.out.println("Total Available: " + totalAvailable);

                // Process late fees first
                if (totalAvailable.compareTo(totalLateFee) >= 0) {
                    // Enough for all late fees, process IOD payments
                    balance = totalAvailable.subtract(totalLateFee);
                    account.setHoldAmount(BigDecimal.ZERO);
                    // In the IOD processing section, modify the transaction creation:
                    // Process IOD payments in order
                    for (RepaymentSchedule overdueSchedule : overdueSchedules) {
                        BigDecimal lateFee = calculateLateFee(overdueSchedule);
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
                            
                            // Only create transaction if there's actual payment
                            if (lateFee.compareTo(BigDecimal.ZERO) > 0 || paidIOD.compareTo(BigDecimal.ZERO) > 0) {
                                RepaymentTransaction transaction = new RepaymentTransaction();
                                transaction.setPaymentDate(Timestamp.valueOf(LocalDateTime.now()));
                                transaction.setPaidLateFee(lateFee);
                                transaction.setPaidIOD(paidIOD);
                                transaction.setLateFeePaidDate(LocalDateTime.now());
                                transaction.setPaidPrincipal(BigDecimal.ZERO);
                                transaction.setPaidInterest(BigDecimal.ZERO);
                                transaction.setRemainingPrincipal(overdueSchedule.getRemainingPrincipal());
                                transaction.setCurrentAccount(account);
                                transaction.setRepaymentSchedule(overdueSchedule);
                                transaction.setStatus(1);
                                repaymentTransactionRepository.save(transaction);
                            }
                            
                            repaymentScheduleRepository.save(overdueSchedule);
                        }
                    }
                } else {
                    // Not enough for late fees, hold the amount
                    account.setHoldAmount(totalAvailable);
                    balance = BigDecimal.ZERO;
                    System.out.println("Holding amount: " + totalAvailable + " for late fees");
                }
            }

            // Update account balance
            account.setBalance(balance);
            currentAccountRepository.save(account);

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
                continue;
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
    }

    private BigDecimal calculateLateFee(RepaymentSchedule schedule) {
        LocalDate today = LocalDate.now();
        LocalDate dueDate = schedule.getDueDate();

        // Get the latest transaction with late fee payment for this schedule
        Optional<RepaymentTransaction> lastLateFeePayment = repaymentTransactionRepository
            .findTopByRepaymentScheduleAndLateFeePaidDateIsNotNullOrderByLateFeePaidDateDesc(schedule);

        // Use late_fee_paid_date if exists, otherwise use due date
        LocalDate startDate = lastLateFeePayment
            .map(transaction -> transaction.getLateFeePaidDate().toLocalDate())
            .orElse(dueDate);

        // Debug information
        System.out.println("Schedule " + schedule.getId() + ":");
        System.out.println("Due Date: " + dueDate);
        System.out.println("Last Late Fee Paid Date: " + (lastLateFeePayment.isPresent() ? 
            lastLateFeePayment.get().getLateFeePaidDate() : "None"));
        System.out.println("Counting late days from: " + startDate);
        System.out.println("Today: " + today);

        // Calculate late days from the appropriate start date
        long lateDays = ChronoUnit.DAYS.between(startDate, today);

        if (lateDays > 0) {
            BigDecimal interestOverDue = schedule.getInterestOverDue();
            BigDecimal ratePercentage = schedule.getSmeLoan().getLate_fee_rate();
            
            if (ratePercentage == null) {
                ratePercentage = new BigDecimal("4.00"); // 4% default rate
            }

            BigDecimal rate = ratePercentage.divide(new BigDecimal("100"));

            System.out.println("Late days: " + lateDays);
            System.out.println("IOD amount: " + interestOverDue);
            System.out.println("Rate (%): " + ratePercentage);
            System.out.println("Rate (decimal): " + rate);

            return interestOverDue.multiply(rate).multiply(BigDecimal.valueOf(lateDays));
        }

        return BigDecimal.ZERO;
    }
}
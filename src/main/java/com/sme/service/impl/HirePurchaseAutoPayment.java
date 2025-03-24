package com.sme.service.impl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sme.entity.CurrentAccount;
import com.sme.entity.HpRegistration;
import com.sme.entity.HpRepaymentTransaction;
import com.sme.entity.HpSchedule;
import com.sme.repository.CurrentAccountRepository;
import com.sme.repository.HpScheduleRepository;
import com.sme.repository.HpTransactionRepository;
import com.sme.repository.HpRegistrationRepository;

import com.sme.service.AutoPaymentStrategy;

@Service
@Transactional
public class HirePurchaseAutoPayment implements AutoPaymentStrategy {
    
    @Autowired
    private HpScheduleRepository hpScheduleRepository;
    
    @Autowired
    private CurrentAccountRepository currentAccountRepository;
    
    @Autowired
    private HpTransactionRepository hpTransactionRepository;
    
    @Autowired
    private HpRegistrationRepository hpRegistrationRepository;

    private CurrentAccount getCurrentAccount(Long hpRegistrationId) {
        return hpRegistrationRepository.findById(hpRegistrationId)
                .map(registration -> {
                    return registration.getCurrentAccount();
                })
                .orElse(null);
    }
    
    @Override
    public void processPayments() {
        LocalDate today = LocalDate.now();
        System.out.println("====== Hire Purchase Auto Pay Starting - " + today + " ======");
        
        try {
            // Find schedules due today or in grace period
            List<HpSchedule> schedulesToProcess = hpScheduleRepository.findSchedulesForProcessing(today);
            
            if (!schedulesToProcess.isEmpty()) {
                boolean isOverdue = schedulesToProcess.stream()
                        .anyMatch(schedule -> today.isAfter(schedule.getDueDate()));
                processSchedules(schedulesToProcess, isOverdue);
            }
            
        } catch (Exception e) {
            System.err.println("Error in HP auto payment process");
            e.printStackTrace();
        }
    }
    
    private void processSchedules(List<HpSchedule> schedules, boolean isOverdue) {
        Map<Long, List<HpSchedule>> schedulesByRegistration = schedules.stream()
                .filter(schedule -> !schedule.getInstallmentNo().equals("0"))  // Filter out the initial row
                .collect(Collectors.groupingBy(HpSchedule::getHpRegistrationId));
        
        for (Map.Entry<Long, List<HpSchedule>> entry : schedulesByRegistration.entrySet()) {
            List<HpSchedule> registrationSchedules = entry.getValue();
            HpSchedule firstSchedule = registrationSchedules.get(0);
            CurrentAccount account = getCurrentAccount(firstSchedule.getHpRegistrationId());
            
            if (account == null) {
                System.out.println("Skipping: No linked account");
                continue;
            }
            
            BigDecimal balance = account.getBalance();
            BigDecimal holdAmount = account.getHoldAmount() != null ? account.getHoldAmount() : BigDecimal.ZERO;
            BigDecimal totalAvailable = balance.add(holdAmount);
            LocalDate today = LocalDate.now();
            
            // Handle grace end date cases even when no balance available
            if (totalAvailable.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("No balance available - Checking for grace end date cases");
                
                for (HpSchedule schedule : registrationSchedules) {
                    if (today.isEqual(schedule.getGraceEndDate())) {
                        // Move both interest and principal to overdue
                        if (schedule.getInterestAmount() > 0) {
                            schedule.setInterestOd(schedule.getInterestOd().add(BigDecimal.valueOf(schedule.getInterestAmount())));
                            schedule.setInterestAmount(0L);
                        }
                        if (schedule.getPrincipalAmount() > 0) {
                            schedule.setPrincipalOd(schedule.getPrincipalOd().add(BigDecimal.valueOf(schedule.getPrincipalAmount())));
                            schedule.setPrincipalAmount(0L);
                        }
                        hpScheduleRepository.save(schedule);
                        System.out.println("Moved amounts to OD for schedule " + schedule.getId());
                    }
                }
                continue;
            }
            
            // Process payments in order
            processPaymentsInOrder(registrationSchedules, account, totalAvailable, isOverdue);
        }
    }
    
    private void processPaymentsInOrder(List<HpSchedule> schedules, CurrentAccount account, 
            BigDecimal availableBalance, boolean isOverdue) {
        
        BigDecimal remainingBalance = availableBalance;
        LocalDate today = LocalDate.now();

        // Sort schedules by due date
        schedules.sort((a, b) -> a.getDueDate().compareTo(b.getDueDate()));

        // Check for 180+ or 90+ days late case first
        long maxLateDays = schedules.stream()
                .filter(s -> s.getStatus() != 6)
                .mapToLong(s -> {
                    LocalDate startDate = s.getLateFeePaidDate() != null ? 
                            s.getLateFeePaidDate() : s.getDueDate();
                    return ChronoUnit.DAYS.between(startDate, today);
                })
                .max()
                .orElse(0);

        List<HpSchedule> lateSchedules = maxLateDays >= 90 ? schedules.stream()
                .filter(s -> s.getStatus() != 6)
                .collect(Collectors.toList())
                : Collections.emptyList();

        if (!lateSchedules.isEmpty()) {
            // Calculate one common late fee for all late terms
            BigDecimal totalOutstanding = calculateTotalOutstanding(lateSchedules);
            BigDecimal totalLateFee;
            
            if (maxLateDays >= 180) {
                totalLateFee = calculate180DaysLateFee(lateSchedules.get(0).getHpRegistrationId(),
                        totalOutstanding, maxLateDays);
                System.out.println("Calculating 180+ days late fee");
            } else {
                totalLateFee = calculate90DaysLateFee(lateSchedules.get(0).getHpRegistrationId(),
                        totalOutstanding, maxLateDays);
                System.out.println("Calculating 90+ days late fee");
            }

            // Process late fee payment if we have enough balance
            if (remainingBalance.compareTo(totalLateFee) >= 0) {
                HpRepaymentTransaction transaction = new HpRepaymentTransaction();
                transaction.setPaymentDate(Timestamp.valueOf(LocalDateTime.now()));
                transaction.setPaidLateFee(totalLateFee.toString());
                transaction.setPaidInterestIOD("0");
                transaction.setPaidPrincipalIOD("0");
                transaction.setPaidInterest("0");
                transaction.setPaidPrincipal("0");
                transaction.setCurrentAccountId(account.getId());
                transaction.setHpScheduleId(lateSchedules.get(0).getId());
                transaction.setStatus(1);
                hpTransactionRepository.save(transaction);

                // Update schedules with late fees
                for (HpSchedule lateSchedule : lateSchedules) {
                    if (lateSchedule.getInterestOd().compareTo(BigDecimal.ZERO) > 0) {
                        lateSchedule.setLateFeePaidDate(today);
                        hpScheduleRepository.save(lateSchedule);
                    }
                }

                remainingBalance = remainingBalance.subtract(totalLateFee);
                account.setBalance(remainingBalance);
            } else {
                // Hold all available money if not enough for late fee
                account.setHoldAmount(remainingBalance);
                account.setBalance(BigDecimal.ZERO);
                currentAccountRepository.save(account);
                return;
            }
        }

        // Process individual schedules
        for (HpSchedule schedule : schedules) {
            if (remainingBalance.compareTo(BigDecimal.ZERO) <= 0) break;
            processIndividualSchedule(schedule, account, remainingBalance, isOverdue);
            remainingBalance = account.getBalance();
        }

        // Final account balance update
        account.setBalance(remainingBalance);
        account.setHoldAmount(BigDecimal.ZERO);
        currentAccountRepository.save(account);
    }

    private BigDecimal calculateTotalOutstanding(List<HpSchedule> schedules) {
        return schedules.stream()
                .map(schedule -> 
                    schedule.getInterestOd()
                        .add(BigDecimal.valueOf(schedule.getInterestAmount()))
                        .add(schedule.getPrincipalOd())
                        .add(BigDecimal.valueOf(schedule.getPrincipalAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculate90DaysLateFee(Long hpRegistrationId, BigDecimal totalOutstanding, long lateDays) {
        // Get rates from HP registration or use default
        BigDecimal ninetyDayRate = new BigDecimal("8.00"); // Default 8%
        
        BigDecimal dailyRate = ninetyDayRate
                .divide(new BigDecimal("100"))
                .divide(new BigDecimal("365"), 10, BigDecimal.ROUND_HALF_UP);

        return totalOutstanding.multiply(dailyRate)
                .multiply(BigDecimal.valueOf(lateDays))
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal calculate180DaysLateFee(Long hpRegistrationId, BigDecimal totalOutstanding, long lateDays) {
        // Get rates from HP registration or use default
        BigDecimal oneEightyDayRate = new BigDecimal("12.00"); // Default 12%
        
        BigDecimal dailyRate = oneEightyDayRate
                .divide(new BigDecimal("100"))
                .divide(new BigDecimal("365"), 10, BigDecimal.ROUND_HALF_UP);

        return totalOutstanding.multiply(dailyRate)
                .multiply(BigDecimal.valueOf(lateDays))
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private void processIndividualSchedule(HpSchedule schedule, CurrentAccount account, BigDecimal remainingBalance, boolean isOverdue) {
        LocalDate today = LocalDate.now();
        HpRepaymentTransaction transaction = new HpRepaymentTransaction();
        transaction.setPaymentDate(Timestamp.valueOf(LocalDateTime.now()));
        transaction.setHpScheduleId(schedule.getId());
        transaction.setCurrentAccountId(account.getId());
        transaction.setStatus(1);
        
        // Initialize all payment fields
        transaction.setPaidLateFee("0");
        transaction.setPaidInterestLateFee("0");
        transaction.setPaidPrincipalLateFee("0");
        transaction.setPaidInterestIOD("0");
        transaction.setPaidPrincipalIOD("0");
        transaction.setPaidInterest("0");
        transaction.setPaidPrincipal("0");

        // 1. Interest Late Fee (for interest OD)
        if (schedule.getInterestOd().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal interestLateFee = calculateInterestLateFee(schedule);
            BigDecimal paidAmount = processPayment(interestLateFee, remainingBalance);
            if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                transaction.setPaidInterestLateFee(paidAmount.toString());
                remainingBalance = remainingBalance.subtract(paidAmount);
            }
        }

        // 2. Principal Late Fee (for principal OD)
        if (schedule.getPrincipalOd().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal principalLateFee = calculatePrincipalLateFee(schedule);
            BigDecimal paidAmount = processPayment(principalLateFee, remainingBalance);
            if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                transaction.setPaidPrincipalLateFee(paidAmount.toString());
                remainingBalance = remainingBalance.subtract(paidAmount);
            }
        }

        // 3. Interest OD
        if (schedule.getInterestOd().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal paidAmount = processPayment(schedule.getInterestOd(), remainingBalance);
            if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                schedule.setInterestOd(schedule.getInterestOd().subtract(paidAmount));
                transaction.setPaidInterestIOD(paidAmount.toString());
                remainingBalance = remainingBalance.subtract(paidAmount);
            }
        }

        // 4. Principal OD
        if (schedule.getPrincipalOd().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal paidAmount = processPayment(schedule.getPrincipalOd(), remainingBalance);
            if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                schedule.setPrincipalOd(schedule.getPrincipalOd().subtract(paidAmount));
                transaction.setPaidPrincipalIOD(paidAmount.toString());
                remainingBalance = remainingBalance.subtract(paidAmount);
            }
        }

        // 5. Interest
        if (schedule.getInterestAmount() > 0) {
            BigDecimal interestAmount = BigDecimal.valueOf(schedule.getInterestAmount());
            BigDecimal paidAmount = processPayment(interestAmount, remainingBalance);
            
            if (today.isEqual(schedule.getGraceEndDate())) {
                // On grace end date, handle partial payment and move remaining to OD
                if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                    // Partial payment made
                    schedule.setInterestAmount(0L); // Clear interest amount
                    transaction.setPaidInterest(paidAmount.toString());
                    remainingBalance = remainingBalance.subtract(paidAmount);
                    
                    // Move unpaid amount to OD
                    BigDecimal unpaidAmount = interestAmount.subtract(paidAmount);
                    if (unpaidAmount.compareTo(BigDecimal.ZERO) > 0) {
                        schedule.setInterestOd(schedule.getInterestOd().add(unpaidAmount));
                    }
                } else {
                    // No payment possible, move entire amount to OD
                    schedule.setInterestAmount(0L);
                    schedule.setInterestOd(schedule.getInterestOd().add(interestAmount));
                }
            } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                // Normal payment processing
                schedule.setInterestAmount(interestAmount.subtract(paidAmount).longValue());
                transaction.setPaidInterest(paidAmount.toString());
                remainingBalance = remainingBalance.subtract(paidAmount);
            }
        }

        // 6. Principal
        if (schedule.getPrincipalAmount() > 0) {
            BigDecimal principalAmount = BigDecimal.valueOf(schedule.getPrincipalAmount());
            BigDecimal paidAmount = processPayment(principalAmount, remainingBalance);
            
            if (today.isEqual(schedule.getGraceEndDate())) {
                // On grace end date, handle partial payment and move remaining to OD
                if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                    // Partial payment made
                    schedule.setPrincipalAmount(0L); // Clear principal amount
                    transaction.setPaidPrincipal(paidAmount.toString());
                    remainingBalance = remainingBalance.subtract(paidAmount);
                    
                    // Move unpaid amount to OD
                    BigDecimal unpaidAmount = principalAmount.subtract(paidAmount);
                    if (unpaidAmount.compareTo(BigDecimal.ZERO) > 0) {
                        schedule.setPrincipalOd(schedule.getPrincipalOd().add(unpaidAmount));
                    }
                } else {
                    // No payment possible, move entire amount to OD
                    schedule.setPrincipalAmount(0L);
                    schedule.setPrincipalOd(schedule.getPrincipalOd().add(principalAmount));
                }
            } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                // Normal payment processing
                schedule.setPrincipalAmount(principalAmount.subtract(paidAmount).longValue());
                transaction.setPaidPrincipal(paidAmount.toString());
                remainingBalance = remainingBalance.subtract(paidAmount);
            }
        }

        // Save transaction if any payment was made
        if (!isZeroTransaction(transaction)) {
            hpTransactionRepository.save(transaction);
        }

        // Update schedule status and save
        updateScheduleStatus(schedule);
        hpScheduleRepository.save(schedule);
        
        // Update account balance
        account.setBalance(remainingBalance);
    }

    private BigDecimal calculateInterestLateFee(HpSchedule schedule) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = schedule.getLateFeePaidDate() != null ? 
                            schedule.getLateFeePaidDate() : 
                            schedule.getDueDate();
        long lateDays = ChronoUnit.DAYS.between(startDate, today);

        if (lateDays <= 0) {
            return BigDecimal.ZERO;
        }

        // Get HP Registration for rates
        HpRegistration registration = hpRegistrationRepository.findById(schedule.getHpRegistrationId())
                .orElseThrow(() -> new RuntimeException("HP Registration not found"));

        BigDecimal ratePercentage = registration.getLate_fee_rate();
        if (ratePercentage == null) {
            ratePercentage = new BigDecimal("3.00"); // Default 3%
        }

        // Calculate daily rate (annual rate / 365)
        BigDecimal dailyRate = ratePercentage
                .divide(new BigDecimal("100"))
                .divide(new BigDecimal("365"), 10, BigDecimal.ROUND_HALF_UP);

        // Calculate late fee based on interest OD amount
        BigDecimal lateFee = schedule.getInterestOd()
                .multiply(dailyRate)
                .multiply(BigDecimal.valueOf(lateDays));

        return lateFee.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal calculatePrincipalLateFee(HpSchedule schedule) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = schedule.getLateFeePaidDate() != null ? 
                            schedule.getLateFeePaidDate() : 
                            schedule.getDueDate();
        long lateDays = ChronoUnit.DAYS.between(startDate, today);

        if (lateDays <= 0) {
            return BigDecimal.ZERO;
        }

        // Get HP Registration for rates
        HpRegistration registration = hpRegistrationRepository.findById(schedule.getHpRegistrationId())
                .orElseThrow(() -> new RuntimeException("HP Registration not found"));

        BigDecimal ratePercentage = registration.getLate_fee_rate();
        if (ratePercentage == null) {
            ratePercentage = new BigDecimal("3.00"); // Default 3%
        }

        // Calculate daily rate (annual rate / 365)
        BigDecimal dailyRate = ratePercentage
                .divide(new BigDecimal("100"))
                .divide(new BigDecimal("365"), 10, BigDecimal.ROUND_HALF_UP);

        // Calculate late fee based on principal OD amount
        BigDecimal lateFee = schedule.getPrincipalOd()
                .multiply(dailyRate)
                .multiply(BigDecimal.valueOf(lateDays));

        return lateFee.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal processPayment(BigDecimal amount, BigDecimal availableBalance) {
        return availableBalance.compareTo(amount) >= 0 ? amount : availableBalance;
    }

    private void updateScheduleStatus(HpSchedule schedule) {
        boolean isFullyPaid = schedule.getLateFee().compareTo(BigDecimal.ZERO) == 0 &&
                             schedule.getInterestOd().compareTo(BigDecimal.ZERO) == 0 &&
                             schedule.getPrincipalOd().compareTo(BigDecimal.ZERO) == 0 &&
                             schedule.getInterestAmount() == 0 &&
                             schedule.getPrincipalAmount() == 0;
        
        if (isFullyPaid) {
            schedule.setStatus(6); // Paid status
        }
    }

    private boolean isZeroTransaction(HpRepaymentTransaction transaction) {
        return new BigDecimal(transaction.getPaidLateFee()).compareTo(BigDecimal.ZERO) == 0 &&
               new BigDecimal(transaction.getPaidInterestLateFee()).compareTo(BigDecimal.ZERO) == 0 &&
               new BigDecimal(transaction.getPaidPrincipalLateFee()).compareTo(BigDecimal.ZERO) == 0 &&
               new BigDecimal(transaction.getPaidInterestIOD()).compareTo(BigDecimal.ZERO) == 0 &&
               new BigDecimal(transaction.getPaidPrincipalIOD()).compareTo(BigDecimal.ZERO) == 0 &&
               new BigDecimal(transaction.getPaidInterest()).compareTo(BigDecimal.ZERO) == 0 &&
               new BigDecimal(transaction.getPaidPrincipal()).compareTo(BigDecimal.ZERO) == 0;
    }
}

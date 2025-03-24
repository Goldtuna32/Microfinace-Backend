package com.sme.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sme.entity.CurrentAccount;
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

        for (HpSchedule schedule : schedules) {
            if (remainingBalance.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            // Payment Priority Order
            // 1. Late Fee
            if (schedule.getLateFee().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal paidAmount = processPayment(schedule.getLateFee(), remainingBalance);
                if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                    schedule.setLateFee(schedule.getLateFee().subtract(paidAmount));
                    remainingBalance = remainingBalance.subtract(paidAmount);
                }
            }

            // 2. Interest Late Fee (if exists in your system)
            // Add this if you have interest late fee

            // 3. Interest OD
            if (schedule.getInterestOd().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal paidAmount = processPayment(schedule.getInterestOd(), remainingBalance);
                if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                    schedule.setInterestOd(schedule.getInterestOd().subtract(paidAmount));
                    remainingBalance = remainingBalance.subtract(paidAmount);
                }
            }

            // 4. Principal OD
            if (schedule.getPrincipalOd().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal paidAmount = processPayment(schedule.getPrincipalOd(), remainingBalance);
                if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                    schedule.setPrincipalOd(schedule.getPrincipalOd().subtract(paidAmount));
                    remainingBalance = remainingBalance.subtract(paidAmount);
                }
            }

            // 5. Interest
            if (schedule.getInterestAmount() > 0) {
                BigDecimal interestAmount = BigDecimal.valueOf(schedule.getInterestAmount());
                BigDecimal paidAmount = processPayment(interestAmount, remainingBalance);
                if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                    schedule.setInterestAmount(interestAmount.subtract(paidAmount).longValue());
                    remainingBalance = remainingBalance.subtract(paidAmount);
                }
            }

            // 6. Principal
            if (schedule.getPrincipalAmount() > 0) {
                BigDecimal principalAmount = BigDecimal.valueOf(schedule.getPrincipalAmount());
                BigDecimal paidAmount = processPayment(principalAmount, remainingBalance);
                if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                    schedule.setPrincipalAmount(principalAmount.subtract(paidAmount).longValue());
                    remainingBalance = remainingBalance.subtract(paidAmount);
                }
            }

            // Update schedule status if fully paid
            updateScheduleStatus(schedule);
            hpScheduleRepository.save(schedule);
        }

        // Update account balance
        account.setBalance(account.getBalance().subtract(availableBalance.subtract(remainingBalance)));
        currentAccountRepository.save(account);
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
}

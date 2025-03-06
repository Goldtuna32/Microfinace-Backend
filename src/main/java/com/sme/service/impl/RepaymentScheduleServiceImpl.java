package com.sme.service.impl;

import com.sme.dto.RepaymentScheduleDTO;
import com.sme.entity.RepaymentSchedule;
import com.sme.entity.SmeLoanRegistration;
import com.sme.repository.RepaymentScheduleRepository;
import com.sme.repository.SmeLoanRegistrationRepository;
import com.sme.service.RepaymentScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RepaymentScheduleServiceImpl implements RepaymentScheduleService {

    @Autowired
    private RepaymentScheduleRepository repaymentScheduleRepository;

    @Autowired
    private SmeLoanRegistrationRepository loanRepository;

    @Transactional
    @Override
    public void generateRepaymentSchedule(Long loanId) {
        SmeLoanRegistration loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.getStatus() != 4) { // Only generate if loan is approved
            throw new RuntimeException("Loan is not approved (Status != 4)");
        }

        BigDecimal loanAmount = loan.getLoanAmount();
        BigDecimal interestRate = loan.getInterestRate().divide(BigDecimal.valueOf(100));
        Long repaymentPeriod = loan.getRepaymentDuration();
        Integer gracePeriod = loan.getGracePeriod();
        LocalDate startDate = loan.getRepaymentStartDate().toLocalDate();

        BigDecimal dailyInterestRate = interestRate.divide(BigDecimal.valueOf(365), 6, BigDecimal.ROUND_HALF_UP);
        BigDecimal remainingBalance = loanAmount; // Initial remaining principal

        List<RepaymentSchedule> schedules = new ArrayList<>();

        for (int i = 1; i <= repaymentPeriod; i++) {
            LocalDate dueDate = startDate.plusMonths(i);
            int daysInMonth = dueDate.lengthOfMonth();

            // Interest is calculated based on the REMAINING PRINCIPAL
            BigDecimal interestAmount = remainingBalance.multiply(dailyInterestRate)
                    .multiply(BigDecimal.valueOf(daysInMonth))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);

            // Principal should remain the full loan amount but we track actual payments
            BigDecimal principalAmount = loanAmount; // Always the full loan amount
            BigDecimal principalPaid = BigDecimal.ZERO; // Principal paid initially zero

            // Check if it's the last term (pay full remaining principal)
            if (i == repaymentPeriod) {
                principalPaid = remainingBalance; // Pay all remaining balance in the last term
                remainingBalance = BigDecimal.ZERO; // Fully paid
            }

            // Create repayment schedule entry
            RepaymentSchedule schedule = new RepaymentSchedule();
            schedule.setSmeLoan(loan);
            schedule.setDueDate(dueDate);
            schedule.setGraceEndDate(dueDate.plusDays(gracePeriod));
            schedule.setInterestAmount(interestAmount);
            schedule.setPrincipalAmount(principalAmount); // Always store full principal
            schedule.setRemainingPrincipal(remainingBalance);
            schedule.setCreatedAt(LocalDate.now().atStartOfDay());
            schedule.setStatus(1); // 1 = Pending payment
            schedule.setPaidLate(false);

            schedules.add(schedule);
        }

        repaymentScheduleRepository.saveAll(schedules);
    }

    @Transactional
    public void updateRepaymentSchedule(Long loanId, BigDecimal paidPrincipal) {
        List<RepaymentSchedule> schedules = repaymentScheduleRepository.findBySmeLoanId(loanId);

        if (schedules.isEmpty()) {
            throw new RuntimeException("Repayment schedule not found for loan ID: " + loanId);
        }

        BigDecimal newRemainingPrincipal = schedules.get(0).getRemainingPrincipal().subtract(paidPrincipal);

        // Update each term's interest dynamically based on the new remaining principal
        for (RepaymentSchedule schedule : schedules) {
            if (schedule.getRemainingPrincipal().compareTo(BigDecimal.ZERO) > 0) {
                int daysInMonth = schedule.getDueDate().lengthOfMonth();
                BigDecimal dailyInterestRate = schedule.getSmeLoan().getInterestRate()
                        .divide(BigDecimal.valueOf(100 * 365), 6, BigDecimal.ROUND_HALF_UP);

                // Recalculate interest based on the new remaining principal
                BigDecimal updatedInterest = newRemainingPrincipal.multiply(dailyInterestRate)
                        .multiply(BigDecimal.valueOf(daysInMonth))
                        .setScale(2, BigDecimal.ROUND_HALF_UP);

                schedule.setInterestAmount(updatedInterest);
                schedule.setRemainingPrincipal(newRemainingPrincipal);

                repaymentScheduleRepository.save(schedule);
            }
        }
    }

    @Override
    public List<RepaymentScheduleDTO> getRepaymentSchedule(Long loanId) {
        List<RepaymentSchedule> schedules = repaymentScheduleRepository.findBySmeLoanId(loanId);
        return schedules.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public RepaymentScheduleDTO convertToDTO(RepaymentSchedule schedule) {
        RepaymentScheduleDTO dto = new RepaymentScheduleDTO();
        dto.setId(schedule.getId());
        dto.setDueDate(schedule.getDueDate());
        dto.setGraceEndDate(schedule.getGraceEndDate());
        dto.setInterestAmount(schedule.getInterestAmount());
        dto.setPrincipalAmount(schedule.getPrincipalAmount());
        // dto.setPrincipal_paid(schedule.getPrincipal_paid()); // Added principalPaid
        // in DTO
        dto.setLateFee(schedule.getLateFee());
        dto.setInterestOverDue(schedule.getInterestOverDue());
        dto.setStatus(schedule.getStatus());
        dto.setRemainingPrincipal(schedule.getRemainingPrincipal());
        dto.setCreatedAt(schedule.getCreatedAt());
        dto.setPaidLate(schedule.getPaidLate());
        // dto.setLateFeePaidDate(schedule.getLateFeePaidDate());
        return dto;
    }
}

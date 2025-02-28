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

        if (loan.getStatus() != 4) {  // Only generate if loan is approved
            throw new RuntimeException("Loan is not approved (Status != 4)");
        }

        BigDecimal loanAmount = loan.getLoanAmount();
        BigDecimal interestRate = loan.getInterestRate().divide(BigDecimal.valueOf(100));
        Long repaymentPeriod = loan.getRepaymentDuration();
        Integer gracePeriod = loan.getGracePeriod(); // Get grace period in days
        LocalDate startDate = loan.getRepaymentStartDate().toLocalDate();

        BigDecimal monthlyInterestRate = interestRate.divide(BigDecimal.valueOf(12), 4, BigDecimal.ROUND_HALF_UP);
        BigDecimal monthlyPayment = loanAmount.multiply(monthlyInterestRate)
                .divide(BigDecimal.ONE.subtract(BigDecimal.ONE
                                .divide((monthlyInterestRate.add(BigDecimal.ONE)).pow(repaymentPeriod.intValue()), 4, BigDecimal.ROUND_HALF_UP)),
                        2, BigDecimal.ROUND_HALF_UP);

        BigDecimal remainingBalance = loanAmount;
        List<RepaymentSchedule> schedules = new ArrayList<>();

        for (int i = 1; i <= repaymentPeriod; i++) {
            BigDecimal interestAmount = remainingBalance.multiply(monthlyInterestRate).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal principalAmount = monthlyPayment.subtract(interestAmount).setScale(2, BigDecimal.ROUND_HALF_UP);
            remainingBalance = remainingBalance.subtract(principalAmount).setScale(2, BigDecimal.ROUND_HALF_UP);

            LocalDate dueDate = startDate.plusMonths(i);
            LocalDate graceEndDate = dueDate.plusDays(gracePeriod); // ✅ Grace period added

            RepaymentSchedule schedule = new RepaymentSchedule();
            schedule.setSmeLoan(loan);
            schedule.setDueDate(dueDate);
            schedule.setGraceEndDate(graceEndDate); // ✅ Store grace end date
            schedule.setInterestAmount(interestAmount);
            schedule.setPrincipalAmount(principalAmount);
            schedule.setRemainingPrincipal(remainingBalance);
            schedule.setCreatedAt(LocalDate.now().atStartOfDay());
            schedule.setStatus(1);  // 1 = Pending payment
            schedule.setPaidLate(false);

            schedules.add(schedule);
        }

        repaymentScheduleRepository.saveAll(schedules);
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
        dto.setLateFee(schedule.getLateFee());
        dto.setInterestOverDue(schedule.getInterestOverDue());
        dto.setStatus(schedule.getStatus());
        dto.setRemainingPrincipal(schedule.getRemainingPrincipal());
        dto.setCreatedAt(schedule.getCreatedAt());
        dto.setPaidLate(schedule.getPaidLate());
        dto.setLateFeePaidDate(schedule.getLateFeePaidDate());
        return dto;
    }
}

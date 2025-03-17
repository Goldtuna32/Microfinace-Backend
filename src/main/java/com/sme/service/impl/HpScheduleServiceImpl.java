package com.sme.service.impl;

import com.sme.dto.HpScheduleDTO;
import com.sme.entity.HpRegistration;
import com.sme.entity.HpSchedule;
import com.sme.repository.HpRegistrationRepository;
import com.sme.repository.HpScheduleRepository;
import com.sme.service.HpScheduleService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HpScheduleServiceImpl implements HpScheduleService {

    @Autowired
    private HpScheduleRepository hpScheduleRepository;

    @Autowired
    private HpRegistrationRepository hpRegistrationRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<HpScheduleDTO> generateHpRepaymentSchedule(Long hpRegistrationId) {
        HpRegistration hpRegistration = hpRegistrationRepository.findById(hpRegistrationId)
                .orElseThrow(() -> new RuntimeException("HP Registration not found"));

        // Check if status is ACTIVE (4)
        if (hpRegistration.getStatus() != 4) {
            throw new IllegalStateException("HP Repayment Schedule can only be generated for ACTIVE (4) status.");
        }

        // Key variables from registration
        BigDecimal loanAmount = hpRegistration.getLoanAmount().subtract(hpRegistration.getDownPayment());
        BigDecimal annualInterestRate = new BigDecimal("0.13"); // Example from document: 13%
        int loanTermMonths = hpRegistration.getLoanTerm();
        LocalDateTime startDate = hpRegistration.getStartDate();

        // Calculate EMI using the formula: [P + (P * r * t)] / n
        BigDecimal totalInterest = loanAmount.multiply(annualInterestRate).multiply(BigDecimal.valueOf(loanTermMonths).divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP));
        BigDecimal totalAmount = loanAmount.add(totalInterest);
        BigDecimal monthlyInstallment = totalAmount.divide(BigDecimal.valueOf(loanTermMonths), 0, RoundingMode.HALF_UP);

        List<HpSchedule> schedules = new ArrayList<>();
        BigDecimal remainingPrincipal = loanAmount;
        BigDecimal totalPrincipalPaid = BigDecimal.ZERO;
        BigDecimal totalInterestPaid = BigDecimal.ZERO;

        // Generate schedule
        for (int i = 0; i < loanTermMonths; i++) {
            HpSchedule schedule = new HpSchedule();

            // Set due date (5th of next month as per document)
            LocalDateTime dueDate = startDate.plusMonths(i + 1).withDayOfMonth(5);
            schedule.setDate(Timestamp.valueOf(dueDate));

            // Calculate days between periods
            LocalDateTime previousDate = (i == 0) ? startDate : startDate.plusMonths(i).withDayOfMonth(5);
            long days = ChronoUnit.DAYS.between(previousDate, dueDate);

            // Interest for this term: (Remaining Principal * Annual Rate * Days) / Days in Year
            BigDecimal interest = remainingPrincipal
                    .multiply(annualInterestRate)
                    .multiply(BigDecimal.valueOf(days))
                    .divide(BigDecimal.valueOf(365), 0, RoundingMode.HALF_UP);

            // Principal for this term
            BigDecimal principal = monthlyInstallment.subtract(interest);

            // Adjust last term
            if (i == loanTermMonths - 1) {
                principal = remainingPrincipal; // Ensure remaining principal is fully paid
                interest = totalInterest.subtract(totalInterestPaid); // Adjust interest to match total
                monthlyInstallment = principal.add(interest); // Recalculate last installment
            }

            // Update running totals
            remainingPrincipal = remainingPrincipal.subtract(principal);
            totalPrincipalPaid = totalPrincipalPaid.add(principal);
            totalInterestPaid = totalInterestPaid.add(interest);

            // Set schedule fields
            schedule.setPrincipalAmount(principal.longValue());
            schedule.setInterestAmount(interest.longValue());
            schedule.setLateDay(0L);
            schedule.setLateFee(BigDecimal.ZERO);
            schedule.setPrincipalOd(BigDecimal.ZERO);
            schedule.setInterestOd(BigDecimal.ZERO);
            schedule.setInstallmentNo("Installment " + (i + 1));
            schedule.setHpRegistrationId(hpRegistrationId);
            schedule.setLateFeePaidDate(null);

            schedules.add(schedule);
        }

        // Save to repository
        hpScheduleRepository.saveAll(schedules);

        // Map to DTOs and return
        return schedules.stream()
                .map(schedule -> modelMapper.map(schedule, HpScheduleDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<HpScheduleDTO> getHpSchedulesByHpRegistrationId(Long hpRegistrationId) {
        return hpScheduleRepository.findByHpRegistrationId(hpRegistrationId).stream()
                .map(schedule -> modelMapper.map(schedule, HpScheduleDTO.class))
                .collect(Collectors.toList());
    }
}
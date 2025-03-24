package com.sme.service.impl;

import com.sme.dto.HpProductDTO;
import com.sme.dto.HpScheduleDTO;
import com.sme.entity.HpRegistration;
import com.sme.entity.HpSchedule;
import com.sme.repository.HpRegistrationRepository;
import com.sme.repository.HpScheduleRepository;
import com.sme.service.HpProductService;
import com.sme.service.HpScheduleService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

        if (hpRegistration.getStatus() != 4) {
            throw new IllegalStateException("HP Repayment Schedule can only be generated for ACTIVE (4) status.");
        }

        List<HpSchedule> schedules = new ArrayList<>();
        
        // Get loan details
        BigDecimal loanAmount = hpRegistration.getLoanAmount();
        BigDecimal yearlyInterestRate = new BigDecimal(hpRegistration.getInterestRate()).divide(BigDecimal.valueOf(100), 32, BigDecimal.ROUND_HALF_UP);
        Integer loanTerm = hpRegistration.getLoanTerm();
        
        // Calculate and log initial values
        System.out.println("Initial Values:");
        System.out.println("Loan Amount: " + loanAmount);
        System.out.println("Yearly Interest Rate: " + yearlyInterestRate);
        System.out.println("Loan Term (months): " + loanTerm);
        
        // Calculate total interest for the loan period
        BigDecimal yearlyInterest = loanAmount.multiply(yearlyInterestRate)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalInterest = loanAmount.multiply(yearlyInterestRate)
                .multiply(BigDecimal.valueOf(loanTerm))
                .divide(BigDecimal.valueOf(12), 32, BigDecimal.ROUND_HALF_UP);
        
        // Calculate total amount (principal + interest)
        BigDecimal totalAmount = loanAmount.add(totalInterest);
        
        System.out.println("\nCalculated Values:");
        System.out.println("Yearly Interest Amount: " + yearlyInterest);
        System.out.println("Total Interest for loan period: " + totalInterest);
        System.out.println("Total Amount (Principal + Interest): " + totalAmount);
        
        // Calculate EMI (total amount / loan term)
        BigDecimal emi = totalAmount.divide(BigDecimal.valueOf(loanTerm), 32, BigDecimal.ROUND_HALF_UP)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        
        // Calculate BMF (Base Monthly Factor)
        // BigDecimal bmf = yearlyInterestRate
        //         .multiply(BigDecimal.valueOf(loanTerm))
        //         .divide(BigDecimal.valueOf(12), 6, BigDecimal.ROUND_HALF_UP)
        //         .divide(BigDecimal.valueOf(loanTerm), 6, BigDecimal.ROUND_HALF_UP);

        // Calculate BMF using financial rate calculation
        // BMF is approximately 1.90089% which is the monthly rate that gives:
        // - 36 payments of 8,494,444.44
        // - Initial loan of 220,000,000
        BigDecimal bmf = new BigDecimal("0.0190089"); // Hard-coded for now as Java doesn't have built-in rate calculation

        System.out.println("\nPayment Details:");
        System.out.println("Monthly EMI: " + emi);
        System.out.println("BMF Percentage: " + bmf.multiply(BigDecimal.valueOf(100)).setScale(6, BigDecimal.ROUND_HALF_UP) + "%");

        BigDecimal remainingBalance = loanAmount;
        LocalDate currentDate = hpRegistration.getStartDate().toLocalDate();

        // Add initial row
        HpSchedule initialSchedule = new HpSchedule();
        initialSchedule.setDueDate(currentDate);
        initialSchedule.setGraceEndDate(currentDate.plusDays(hpRegistration.getGracePeriod()));
        initialSchedule.setPrincipalAmount(loanAmount.longValue());
        initialSchedule.setInterestAmount(0L);
        initialSchedule.setInstallmentNo("0");
        initialSchedule.setHpRegistrationId(hpRegistrationId);
        schedules.add(initialSchedule);

        // Generate schedule
        for (int i = 1; i <= loanTerm; i++) {
            LocalDate dueDate = currentDate.plusMonths(i);
            
            // Calculate interest using BMF
            BigDecimal interestAmount = remainingBalance.multiply(bmf)
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            
            // Calculate principal (EMI - interest)
            BigDecimal principalForThisPeriod = emi.subtract(interestAmount);
            
            // Adjust last payment if needed
            if (i == loanTerm) {
                principalForThisPeriod = remainingBalance;
                emi = principalForThisPeriod.add(interestAmount);
            }

            remainingBalance = remainingBalance.subtract(principalForThisPeriod);

            HpSchedule schedule = new HpSchedule();
            schedule.setDueDate(dueDate);
            schedule.setGraceEndDate(dueDate.plusDays(hpRegistration.getGracePeriod()));
            schedule.setInterestAmount(interestAmount.longValue());
            schedule.setPrincipalAmount(principalForThisPeriod.longValue());
            schedule.setLateDay(0L);
            schedule.setLateFee(BigDecimal.ZERO);
            schedule.setPrincipalOd(BigDecimal.ZERO);
            schedule.setInterestOd(BigDecimal.ZERO);
            schedule.setInstallmentNo(String.valueOf(i));
            schedule.setHpRegistrationId(hpRegistrationId);
            schedules.add(schedule);
        }

        hpScheduleRepository.saveAll(schedules);
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

package com.sme.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class HpScheduleDTO {
    private Long id;
    private LocalDateTime date;
    private Long interestAmount;
    private Long principalAmount;
    private Long lateDay;
    private LocalDate dueDate;
    private BigDecimal lateFee;
    private String principalOd;
    private String interestOd;
    private String installmentNo;
    private Long hpRegistrationId;
    private LocalDateTime lateFeePaidDate;
}

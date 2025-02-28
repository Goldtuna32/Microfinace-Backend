package com.sme.service;

import com.sme.dto.RepaymentScheduleDTO;
import com.sme.entity.RepaymentSchedule;
import com.sme.entity.SmeLoanRegistration;
import com.sme.repository.RepaymentScheduleRepository;
import com.sme.repository.SmeLoanRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public interface RepaymentScheduleService {

   void generateRepaymentSchedule(Long loanId);

   List<RepaymentScheduleDTO> getRepaymentSchedule(Long loanId);

   RepaymentScheduleDTO convertToDTO(RepaymentSchedule schedule);
}

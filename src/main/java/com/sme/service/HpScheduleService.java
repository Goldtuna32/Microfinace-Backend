package com.sme.service;

import com.sme.dto.HpScheduleDTO;
import com.sme.entity.HpRegistration;
import com.sme.entity.HpSchedule;
import com.sme.repository.HpRegistrationRepository;
import com.sme.repository.HpScheduleRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public interface HpScheduleService {

    List<HpScheduleDTO> generateHpRepaymentSchedule(Long hpRegistrationId);
    List<HpScheduleDTO> getHpSchedulesByHpRegistrationId(Long hpRegistrationId);
}

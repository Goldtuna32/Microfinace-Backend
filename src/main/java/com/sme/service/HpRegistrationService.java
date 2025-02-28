package com.sme.service;

import com.sme.dto.HpRegistrationDTO;
import com.sme.entity.HpRegistration;
import com.sme.repository.HpRegistrationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public interface HpRegistrationService {

    List<HpRegistrationDTO> getAllHpRegistrations();

    HpRegistrationDTO getHpRegistrationById(Long id);

    HpRegistrationDTO createHpRegistration(HpRegistrationDTO dto);

    HpRegistrationDTO updateHpRegistration(Long id, HpRegistrationDTO dto);

    void deleteHpRegistration(Long id);
}

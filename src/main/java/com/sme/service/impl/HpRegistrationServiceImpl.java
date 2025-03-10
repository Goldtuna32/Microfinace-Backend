package com.sme.service.impl;

import com.sme.dto.HpRegistrationDTO;
import com.sme.entity.HpRegistration;
import com.sme.repository.HpRegistrationRepository;
import com.sme.service.HpRegistrationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HpRegistrationServiceImpl implements HpRegistrationService {

    @Autowired
    private HpRegistrationRepository repository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<HpRegistrationDTO> getAllHpRegistrations() {
        List<HpRegistration> hpRegistrations = repository.findAll();
        return hpRegistrations.stream()
                .map(hp -> modelMapper.map(hp, HpRegistrationDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public HpRegistrationDTO getHpRegistrationById(Long id) {
        HpRegistration hpRegistration = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hp Registration not found"));
        return modelMapper.map(hpRegistration, HpRegistrationDTO.class);
    }

    @Override
    public HpRegistrationDTO createHpRegistration(HpRegistrationDTO dto) {
        HpRegistration hpRegistration = modelMapper.map(dto, HpRegistration.class);
        hpRegistration.setCreatedDate(java.time.LocalDateTime.now());
        HpRegistration savedHpRegistration = repository.save(hpRegistration);
        return modelMapper.map(savedHpRegistration, HpRegistrationDTO.class);
    }

    @Override
    public HpRegistrationDTO updateHpRegistration(Long id, HpRegistrationDTO dto) {
        HpRegistration existingHp = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hp Registration not found"));

        if (dto.getHpNumber() != null) existingHp.setHpNumber(dto.getHpNumber());
        if (dto.getLoanAmount() != null) existingHp.setLoanAmount(dto.getLoanAmount());
        if (dto.getDownPayment() != null) existingHp.setDownPayment(dto.getDownPayment());
        if (dto.getLoanTerm() != null) existingHp.setLoanTerm(dto.getLoanTerm());
        if (dto.getInterestRate() != null) existingHp.setInterestRate(dto.getInterestRate());
        if (dto.getStartDate() != null) existingHp.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) existingHp.setEndDate(dto.getEndDate());
        if (dto.getStatus() != null) existingHp.setStatus(dto.getStatus());
//        if (dto.getCurrentAccountId() != null) existingHp.setCurrentAccountId(dto.getCurrentAccountId());
//        if (dto.getHpProductId() != null) existingHp.setHpProductId(dto.getHpProductId());

        HpRegistration updatedHp = repository.save(existingHp);
        return modelMapper.map(updatedHp, HpRegistrationDTO.class);
    }

    @Override
    public void deleteHpRegistration(Long id) {
        repository.deleteById(id);
    }
}

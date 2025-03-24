package com.sme.service.impl;

import com.sme.dto.HpRegistrationDTO;
import com.sme.entity.CurrentAccount;
import com.sme.entity.HpProduct;
import com.sme.entity.HpRegistration;
import com.sme.repository.CurrentAccountRepository;
import com.sme.repository.HpProductRepository;
import com.sme.repository.HpRegistrationRepository;
import com.sme.service.HpRegistrationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    @Autowired
    private HpProductRepository hpProductRepository;

    @Override
    public List<HpRegistrationDTO> getAllHpRegistrations() {
        List<HpRegistration> hpRegistrations = repository.findAll();
        List<HpRegistrationDTO> dtos = new ArrayList<>();

        for (HpRegistration hp : hpRegistrations) {
            HpRegistrationDTO dto = new HpRegistrationDTO();
            dto.setId(hp.getId());
            dto.setHpNumber(hp.getHpNumber());
            dto.setCreatedDate(hp.getCreatedDate());
            dto.setLoanAmount(hp.getLoanAmount());
            dto.setDownPayment(hp.getDownPayment());
            dto.setLoanTerm(hp.getLoanTerm());
            dto.setInterestRate(hp.getInterestRate());
            dto.setStatus(hp.getStatus());

            // Mapping related entities (assuming IDs are sufficient)
            if (hp.getCurrentAccount() != null) {
                dto.setCurrentAccount(hp.getCurrentAccount().getId());
            }
            if (hp.getHpProduct() != null) {
                dto.setHpProduct(hp.getHpProduct().getId());
            }

            dtos.add(dto);
        }

        return dtos;
    }

    @Override
    public HpRegistrationDTO getHpRegistrationById(Long id) {
        HpRegistration hpRegistration = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hp Registration not found"));
        return modelMapper.map(hpRegistration, HpRegistrationDTO.class);
    }

    @Override
    @Transactional
    public HpRegistrationDTO createHpRegistration(HpRegistrationDTO dto) {
        HpRegistration hpRegistration = new HpRegistration();

        // Manually setting fields from DTO
        hpRegistration.setHpNumber(dto.getHpNumber());
        hpRegistration.setLoanAmount(dto.getLoanAmount());
        hpRegistration.setDownPayment(dto.getDownPayment());
        hpRegistration.setLoanTerm(dto.getLoanTerm());
        hpRegistration.setInterestRate(dto.getInterestRate());
        hpRegistration.setStatus(dto.getStatus());

        // Setting created and end date
        hpRegistration.setCreatedDate(LocalDateTime.now());
        hpRegistration.setEndDate(LocalDateTime.now());

        // Fetch and set CurrentAccount if ID is provided
        if (dto.getCurrentAccount() != null) {
            CurrentAccount currentAccount = currentAccountRepository.findById(dto.getCurrentAccount())
                    .orElseThrow(() -> new RuntimeException("Current Account not found"));
            hpRegistration.setCurrentAccount(currentAccount);
        }

        // Fetch and set HpProduct if ID is provided
        if (dto.getHpProduct() != null) {
            HpProduct hpProduct = hpProductRepository.findById(dto.getHpProduct())
                    .orElseThrow(() -> new RuntimeException("HP Product not found"));
            hpRegistration.setHpProduct(hpProduct);
        }

        // Save to repository
        HpRegistration savedHpRegistration = repository.save(hpRegistration);

        // Manually map back to DTO
        HpRegistrationDTO responseDto = new HpRegistrationDTO();
        responseDto.setId(savedHpRegistration.getId());
        responseDto.setHpNumber(savedHpRegistration.getHpNumber());
        responseDto.setCreatedDate(savedHpRegistration.getCreatedDate());
        responseDto.setLoanAmount(savedHpRegistration.getLoanAmount());
        responseDto.setDownPayment(savedHpRegistration.getDownPayment());
        responseDto.setLoanTerm(savedHpRegistration.getLoanTerm());
        responseDto.setInterestRate(savedHpRegistration.getInterestRate());
        responseDto.setStatus(savedHpRegistration.getStatus());
        responseDto.setCurrentAccount(savedHpRegistration.getCurrentAccount().getId());
        responseDto.setHpProduct(savedHpRegistration.getHpProduct().getId());

        return responseDto;
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
    public void softDeleteHpRegistration(Long id) {
        HpRegistration hpRegistration = repository.findById(id).orElseThrow(() -> new RuntimeException("HP Registration not found"));
        hpRegistration.setStatus(2);  // Mark as deleted (status = 0)
        repository.save(hpRegistration);
    }


    @Override
    public void restoreHpRegistration(Long id) {
        HpRegistration hpRegistration = repository.findById(id).orElseThrow(() -> new RuntimeException("HP Registration not found"));
        hpRegistration.setStatus(1);  // Mark as active (status = 1)
        repository.save(hpRegistration);
    }


    @Override
    public HpRegistrationDTO save(HpRegistrationDTO hpDto) {
        return createHpRegistration(hpDto);
    }

}

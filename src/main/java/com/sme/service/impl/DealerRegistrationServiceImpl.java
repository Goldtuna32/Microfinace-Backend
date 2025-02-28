package com.sme.service.impl;

import com.sme.dto.DealerRegistrationDTO;
import com.sme.entity.DealerRegistration;
import com.sme.repository.DealerRegistrationRepository;
import com.sme.service.DealerRegistrationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DealerRegistrationServiceImpl implements DealerRegistrationService {

    @Autowired
    private DealerRegistrationRepository dealerRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public DealerRegistrationDTO createDealer(DealerRegistrationDTO dealerDTO) {
        DealerRegistration dealer = modelMapper.map(dealerDTO, DealerRegistration.class);
        dealer.setRegistrationDate(LocalDateTime.now());
        DealerRegistration savedDealer = dealerRepository.save(dealer);
        return modelMapper.map(savedDealer, DealerRegistrationDTO.class);
    }

    @Override
    public List<DealerRegistrationDTO> getAllDealers() {
        List<DealerRegistration> dealers = dealerRepository.findAll();
        return dealers.stream()
                .map(dealer -> modelMapper.map(dealer, DealerRegistrationDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public DealerRegistrationDTO getDealerById(Long id) {
        DealerRegistration dealer = dealerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dealer not found with ID: " + id));
        return modelMapper.map(dealer, DealerRegistrationDTO.class);
    }

    @Override
    @Transactional
    public DealerRegistrationDTO updateDealer(Long id, DealerRegistrationDTO dealerDTO) {
        DealerRegistration existingDealer = dealerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dealer not found with ID: " + id));

        existingDealer.setCompanyName(dealerDTO.getCompanyName());
        existingDealer.setPhoneNumber(dealerDTO.getPhoneNumber());
        existingDealer.setStatus(dealerDTO.getStatus());
        existingDealer.setAddressId(dealerDTO.getAddressId());
        existingDealer.setCurrentAccountId(dealerDTO.getCurrentAccountId());

        DealerRegistration updatedDealer = dealerRepository.save(existingDealer);
        return modelMapper.map(updatedDealer, DealerRegistrationDTO.class);
    }

    @Override
    @Transactional
    public void deleteDealer(Long id) {
        DealerRegistration dealer = dealerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dealer not found with ID: " + id));
        dealerRepository.delete(dealer);
    }
}

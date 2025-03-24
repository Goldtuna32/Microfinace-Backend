package com.sme.service.impl;

import com.sme.dto.DealerRegistrationDTO;
import com.sme.entity.Address;
import com.sme.entity.CurrentAccount;
import com.sme.entity.DealerRegistration;
import com.sme.exception.*;
import com.sme.repository.AddressRepository;
import com.sme.repository.CurrentAccountRepository;
import com.sme.repository.DealerRegistrationRepository;
import com.sme.service.DealerRegistrationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DealerRegistrationServiceImpl implements DealerRegistrationService {

    @Autowired
    private DealerRegistrationRepository dealerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    @Override
    @Transactional
    public DealerRegistrationDTO createDealer(DealerRegistrationDTO dto) {
        validateDealerDTO(dto);

        if (dto.getCompanyName() != null && dealerRepository.existsByCompanyName(dto.getCompanyName())) {
            throw new DuplicateDealerException(dto.getCompanyName());
        }

        try {
            Address address = modelMapper.map(dto.getAddress(), Address.class);
            try {
                address = addressRepository.save(address);
            } catch (Exception e) {
                throw new AddressPersistenceException(
                        "Failed to save address for dealer: " + dto.getCompanyName(), e);
            }

            CurrentAccount currentAccount = currentAccountRepository.findById(dto.getCurrentAccountId())
                    .orElseThrow(() -> new CurrentAccountNotFoundException(dto.getCurrentAccountId()));

            DealerRegistration dealer = modelMapper.map(dto, DealerRegistration.class);
            dealer.setAddress(address);
            dealer.setCurrentAccount(currentAccount);
            dealer.setRegistrationDate(LocalDateTime.now());
            dealer.setStatus(1); // Default to active

            dealer = dealerRepository.save(dealer);
            return modelMapper.map(dealer, DealerRegistrationDTO.class);
        } catch (Exception e) {
            throw new DealerCreationException(
                    "Failed to create dealer: " + dto.getCompanyName(), e);
        }
    }


    @Override
    @Transactional
    public DealerRegistrationDTO updateDealer(Long id, DealerRegistrationDTO dto) {
        DealerRegistration dealer = dealerRepository.findById(id)
                .orElseThrow(() -> new DealerNotFoundException(id));

        validateDealerDTO(dto);

        // Check for duplicate company name, excluding current dealer
        if (dto.getCompanyName() != null &&
                !dto.getCompanyName().equals(dealer.getCompanyName()) &&
                dealerRepository.existsByCompanyName(dto.getCompanyName())) {
            throw new DuplicateDealerException(dto.getCompanyName());
        }

        try {
            dealer.setCompanyName(dto.getCompanyName());
            dealer.setPhoneNumber(dto.getPhoneNumber());
            dealer.setStatus(dto.getStatus());

            Address address = dealer.getAddress();
            address.setStreet(dto.getAddress().getStreet());
            address.setDistrict(dto.getAddress().getDistrict());
            address.setTownship(dto.getAddress().getTownship());
            try {
                address = addressRepository.save(address);
            } catch (Exception e) {
                throw new AddressPersistenceException(
                        "Failed to update address for dealer with id: " + id, e);
            }

            dealer.setAddress(address);
            dealer = dealerRepository.save(dealer);
            return modelMapper.map(dealer, DealerRegistrationDTO.class);
        } catch (Exception e) {
            throw new DealerUpdateException(
                    "Failed to update dealer with id: " + id, e);
        }
    }

    @Override
    public DealerRegistrationDTO getDealer(Long id) {
        DealerRegistration dealer = dealerRepository.findById(id)
                .orElseThrow(() -> new DealerNotFoundException(id));
        return modelMapper.map(dealer, DealerRegistrationDTO.class);
    }

    @Override
    public List<DealerRegistrationDTO> getAllDealerRegistrations() {
        List<DealerRegistration> dealers = dealerRepository.findAll();
        return dealers.stream()
                .map(dealer -> modelMapper.map(dealer, DealerRegistrationDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteDealer(Long id) {
        if (!dealerRepository.existsById(id)) {
            throw new DealerNotFoundException(id);
        }
        try {
            dealerRepository.deleteById(id);
        } catch (Exception e) {
            throw new DealerUpdateException(
                    "Failed to delete dealer with id: " + id, e);
        }
    }

    private void validateDealerDTO(DealerRegistrationDTO dto) {
        // Required field validation
        if (dto.getCompanyName() == null || dto.getCompanyName().trim().isEmpty()) {
            throw new MissingRequiredFieldException("companyName");
        }
        if (dto.getCurrentAccountId() == null) {
            throw new MissingRequiredFieldException("currentAccountId");
        }
        if (dto.getAddress() == null) {
            throw new MissingRequiredFieldException("address");
        }
        if (dto.getAddress().getStreet() == null || dto.getAddress().getStreet().trim().isEmpty()) {
            throw new MissingRequiredFieldException("address.street");
        }

        // Phone number validation (example: 9-12 digits)
        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().matches("\\d{9,12}")) {
            throw new InvalidPhoneNumberException(dto.getPhoneNumber());
        }

        // Company name length validation (example: max 100 characters)
        if (dto.getCompanyName().length() > 100) {
            throw new DealerValidationException(
                    "Company name exceeds maximum length of 100 characters");
        }

        // Status validation (if provided)
        if (dto.getStatus() != null && (dto.getStatus() != 1 && dto.getStatus() != 2)) {
            throw new DealerValidationException(
                    "Invalid status value: " + dto.getStatus() + " (must be 1 or 2)");
        }
    }
}

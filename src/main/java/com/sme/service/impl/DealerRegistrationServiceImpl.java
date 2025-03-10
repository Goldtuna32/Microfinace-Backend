package com.sme.service.impl;

import com.sme.dto.DealerRegistrationDTO;
import com.sme.entity.Address;
import com.sme.entity.CurrentAccount;
import com.sme.entity.DealerRegistration;
import com.sme.repository.AddressRepository;
import com.sme.repository.CurrentAccountRepository;
import com.sme.repository.DealerRegistrationRepository;
import com.sme.service.DealerRegistrationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public DealerRegistrationDTO createDealer(DealerRegistrationDTO dto) {
        // Step 1: Save the Address first
        Address address = modelMapper.map(dto.getAddress(), Address.class);
        address = addressRepository.save(address);

        // Step 2: Retrieve the CurrentAccount
        if (dto.getCurrentAccountId() != null) {
            CurrentAccount currentAccount = currentAccountRepository.findById(dto.getCurrentAccountId())
                    .orElseThrow(() -> new RuntimeException("CurrentAccount not found"));

            // Step 3: Create DealerRegistration and set the Address and CurrentAccount
            DealerRegistration dealer = modelMapper.map(dto, DealerRegistration.class);
            dealer.setAddress(address); // Set the Address foreign key
            dealer.setCurrentAccount(currentAccount); // Set the CurrentAccount foreign key

            dealer = dealerRepository.save(dealer);

            // Step 4: Return the saved DealerRegistration as DTO
            return modelMapper.map(dealer, DealerRegistrationDTO.class);
        } else {
            throw new RuntimeException("CurrentAccount information is required");
        }
    }


    @Override
    public DealerRegistrationDTO updateDealer(Long id, DealerRegistrationDTO dto) {
        DealerRegistration dealer = dealerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dealer not found"));

        // Update Dealer fields
        dealer.setCompanyName(dto.getCompanyName());
        dealer.setPhoneNumber(dto.getPhoneNumber());
        dealer.setStatus(dto.getStatus());

        // Update Address if needed
        Address address = dealer.getAddress();
        address.setStreet(dto.getAddress().getStreet());
        address.setDistrict(dto.getAddress().getDistrict());
        address.setTownship(dto.getAddress().getTownship());
        address = addressRepository.save(address); // Save updated Address

        dealer.setAddress(address); // Link updated Address
        dealer = dealerRepository.save(dealer); // Save updated DealerRegistration

        return modelMapper.map(dealer, DealerRegistrationDTO.class);
    }

    @Override
    public DealerRegistrationDTO getDealer(Long id) {
        DealerRegistration dealer = dealerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dealer not found"));
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
        dealerRepository.deleteById(id);
    }
}

package com.sme.service;

import com.sme.dto.DealerRegistrationDTO;
import com.sme.entity.DealerRegistration;
import com.sme.repository.DealerRegistrationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


public interface DealerRegistrationService {

   DealerRegistrationDTO createDealer(DealerRegistrationDTO dealerDTO);

   List<DealerRegistrationDTO> getAllDealers();

   DealerRegistrationDTO getDealerById(Long id);

   DealerRegistrationDTO updateDealer(Long id, DealerRegistrationDTO dealerDTO);

   void deleteDealer(Long id);
}

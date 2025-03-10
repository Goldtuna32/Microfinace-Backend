package com.sme.service;

import com.sme.dto.DealerRegistrationDTO;
import com.sme.entity.DealerRegistration;

import java.util.List;

public interface DealerRegistrationService {
   DealerRegistrationDTO createDealer(DealerRegistrationDTO dto);
   DealerRegistrationDTO updateDealer(Long id, DealerRegistrationDTO dto);
   DealerRegistrationDTO getDealer(Long id);
   void deleteDealer(Long id);
   List<DealerRegistrationDTO> getAllDealerRegistrations();
}

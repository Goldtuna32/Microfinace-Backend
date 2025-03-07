package com.sme.service;

import com.sme.dto.LoanRegistrationRequest;
import com.sme.dto.SmeLoanRegistrationDTO;
import com.sme.entity.Collateral;
import com.sme.entity.SmeLoanCollateral;
import com.sme.entity.SmeLoanRegistration;
import com.sme.repository.SmeLoanCollateralRepository;
import com.sme.repository.SmeLoanRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;



public interface SmeLoanRegistrationService {

    SmeLoanRegistrationDTO getLoanById(Long id);

    SmeLoanRegistrationDTO registerLoan(LoanRegistrationRequest request);

    SmeLoanRegistrationDTO updateLoan(Long id, SmeLoanRegistrationDTO dto);

    List<SmeLoanRegistrationDTO> getApprovedLoans();

    List<SmeLoanRegistrationDTO> getPendingLoans();

    SmeLoanRegistrationDTO approveLoan(Long id);


}

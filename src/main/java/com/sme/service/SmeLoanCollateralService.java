package com.sme.service;

import com.sme.dto.SmeLoanCollateralDTO;
import com.sme.entity.Collateral;
import com.sme.entity.SmeLoanCollateral;
import com.sme.entity.SmeLoanRegistration;
import com.sme.repository.CollateralRepository;
import com.sme.repository.SmeLoanCollateralRepository;
import com.sme.repository.SmeLoanRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


public interface SmeLoanCollateralService {

    SmeLoanCollateralDTO linkCollateralToLoan(SmeLoanCollateralDTO dto);

//    List<SmeLoanCollateralDTO> getCollateralsForLoan(Long loanId);
//
//    List<SmeLoanCollateralDTO> getLoansForCollateral(Long collateralId);
}

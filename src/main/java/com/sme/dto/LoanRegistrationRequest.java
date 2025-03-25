package com.sme.dto;

import com.sme.entity.SmeLoanCollateral;
import com.sme.entity.SmeLoanRegistration;
import lombok.Data;
import java.util.List;

@Data
public class LoanRegistrationRequest {
    private SmeLoanRegistrationDTO loan;
    private List<SmeLoanCollateralDTO> collaterals;
}

package com.sme.service.impl;

import com.sme.dto.SmeLoanRegistrationDTO;
import com.sme.entity.SmeLoanRegistration;
import com.sme.repository.SmeLoanRegistrationRepository;
import com.sme.service.SmeLoanReportService;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SmeLoanReportServiceImpl implements SmeLoanReportService {

    private final SmeLoanRegistrationRepository loanRepository;

    @Override
    public byte[] generateLoanReport(Long loanId, String format) throws JRException {
        SmeLoanRegistration loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + loanId));

        // Map to DTO
        SmeLoanRegistrationDTO dto = new SmeLoanRegistrationDTO();
        dto.setId(loan.getId());
        dto.setSerialCode(loan.getSerialCode());
        dto.setLoanAmount(loan.getLoanAmount());
        dto.setInterestRate(loan.getInterestRate());
        dto.setGracePeriod(loan.getGracePeriod());
        dto.setRepaymentDuration(loan.getRepaymentDuration());
        dto.setDocumentFee(loan.getDocumentFee());
        dto.setServiceCharges(loan.getServiceCharges());
        dto.setStatus(loan.getStatus());
        dto.setDueDate(loan.getDueDate());
        dto.setRepaymentStartDate(loan.getRepaymentStartDate());
        dto.setAccountNumber(loan.getSerialCode());
        dto.setTotalCollateralAmount(loan.getTotalCollateralAmount());

        InputStream reportStream = getClass().getResourceAsStream("/reports/sme_loan_report.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(dto));
        Map<String, Object> parameters = new HashMap<>();

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        if ("pdf".equalsIgnoreCase(format)) {
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } else {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }
}

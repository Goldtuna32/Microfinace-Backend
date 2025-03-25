package com.sme.service;

import net.sf.jasperreports.engine.JRException;
import java.io.IOException;

public interface SmeLoanReportService {
    byte[] generateLoanReport(Long loanId, String format) throws JRException, IOException;
}

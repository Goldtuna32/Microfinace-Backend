package com.sme.service;

public interface ReportService {
    byte[] generateActiveCIFReport(String format) throws Exception;
    byte[] generateDeletedCIFReport(String format) throws Exception;
    byte[] generateTransactionReport(String format) throws Exception;
    byte[] generateCIFDetailReport(Long cifId, String format) throws Exception;
    byte[] generateLoanDetailReport(Long loanId, String format) throws Exception;
    byte[] generateHpDetailReport(Long hpId, String format) throws Exception;
    byte[] generateHpScheduleReport(Long hpRegistrationId, String format) throws Exception;
}
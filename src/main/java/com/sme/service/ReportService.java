package com.sme.service;

public interface ReportService {
    byte[] generateActiveCIFReport(String format) throws Exception;
    byte[] generateDeletedCIFReport(String format) throws Exception;
}
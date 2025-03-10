package com.sme.controller;

import com.sme.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping(value = "/cif/active/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateActiveCIFPdfReport() throws Exception {
        byte[] reportBytes = reportService.generateActiveCIFReport("pdf");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "active_cifs_report.pdf");
        headers.setContentLength(reportBytes.length);

        return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);
    }

    @GetMapping(value = "/cif/active/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> generateActiveCIFExcelReport() throws Exception {
        byte[] reportBytes = reportService.generateActiveCIFReport("excel");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "active_cifs_report.xlsx");
        headers.setContentLength(reportBytes.length);

        return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);
    }

    @GetMapping(value = "/cif/deleted/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateDeletedCIFPdfReport() throws Exception {
        byte[] reportBytes = reportService.generateDeletedCIFReport("pdf");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "deleted_cifs_report.pdf");
        headers.setContentLength(reportBytes.length);
        return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);
    }

    @GetMapping(value = "/cif/deleted/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> generateDeletedCIFExcelReport() throws Exception {
        byte[] reportBytes = reportService.generateDeletedCIFReport("excel");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "deleted_cifs_report.xlsx");
        headers.setContentLength(reportBytes.length);
        return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);
    }
}

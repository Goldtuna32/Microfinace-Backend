package com.sme.controller;

import com.sme.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/transactions")
    public ResponseEntity<Resource> generateReport(@RequestParam String format) throws Exception {
        byte[] reportBytes = reportService.generateTransactionReport(format);

        String contentType = format.equals("pdf") ? "application/pdf" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        String fileExtension = format.equals("pdf") ? "pdf" : "xlsx";

        ByteArrayResource resource = new ByteArrayResource(reportBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transaction_report." + fileExtension)
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @GetMapping(value = "/cif/detail/{cifId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateCIFDetailPdfReport(@PathVariable Long cifId) throws Exception {
        byte[] reportBytes = reportService.generateCIFDetailReport(cifId, "pdf");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "cif_detail_report_" + cifId + ".pdf");
        headers.setContentLength(reportBytes.length);
        return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);
    }

    @GetMapping(value = "/cif/detail/{cifId}/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> generateCIFDetailExcelReport(@PathVariable Long cifId) throws Exception {
        byte[] reportBytes = reportService.generateCIFDetailReport(cifId, "excel");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "cif_detail_report_" + cifId + ".xlsx");
        headers.setContentLength(reportBytes.length);
        return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);
    }
}

package com.sme.controller;

import com.sme.service.ReportService;
import com.sme.service.SmeLoanReportService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private SmeLoanReportService smeLoanReportService;

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

//    @GetMapping("/hp-product")
//    public ResponseEntity<Resource> generateHpProductReport(
//            @RequestParam Long dealerRegistrationId,
//            @RequestParam String format) throws Exception {
//
//        byte[] reportBytes = reportService.generateHpProductReport(dealerRegistrationId, format);
//        String contentType = format.equals("pdf") ? MediaType.APPLICATION_PDF_VALUE :
//                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
//        String extension = format.equals("pdf") ? "pdf" : "xlsx";
//
//        ByteArrayResource resource = new ByteArrayResource(reportBytes);
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=hp_product_report." + extension)
//                .contentType(MediaType.parseMediaType(contentType))
//                .body(resource);
//    }

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<byte[]> generateLoanReport(@PathVariable Long loanId,
                                                     @RequestParam String format) {
        try {
            byte[] report = smeLoanReportService.generateLoanReport(loanId, format);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=loan_report." + format);

            return new ResponseEntity<>(report, headers, HttpStatus.OK);
        } catch (JRException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping(value = "/loan/detail/{loanId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateLoanDetailPdfReport(@PathVariable Long loanId) throws Exception {
        byte[] reportBytes = reportService.generateLoanDetailReport(loanId, "pdf");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                String.format("loan_detail_%d.pdf", loanId));
        headers.setContentLength(reportBytes.length);

        return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);
    }

    @GetMapping(value = "/loan/detail/{loanId}/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> generateLoanDetailExcelReport(@PathVariable Long loanId) throws Exception {
        byte[] reportBytes = reportService.generateLoanDetailReport(loanId, "excel");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment",
                String.format("loan_detail_%d.xlsx", loanId));
        headers.setContentLength(reportBytes.length);

        return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);
    }
}

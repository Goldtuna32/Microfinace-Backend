package com.sme.controller;

import com.sme.service.RepaymentScheduleReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class RepaymentScheduleReportController {

    @Autowired
    private RepaymentScheduleReportService reportService;

    @GetMapping("/repayment-schedule/{loanId}")
    public ResponseEntity<byte[]> generateReport(@PathVariable Long loanId, @RequestParam String format) throws Exception {
        byte[] reportData = reportService.generateReport(loanId, format);

        HttpHeaders headers = new HttpHeaders();
        if (format.equalsIgnoreCase("pdf")) {
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "repayment_schedule.pdf");
        } else if (format.equalsIgnoreCase("excel")) {
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "repayment_schedule.xlsx");
        }

        return ResponseEntity.ok().headers(headers).body(reportData);
    }
}

package com.sme.service;

import com.sme.entity.RepaymentSchedule;
import com.sme.repository.RepaymentScheduleRepository;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RepaymentScheduleReportService {

    @Autowired
    private RepaymentScheduleRepository repaymentScheduleRepository;

    public byte[] generateReport(Long loanId, String format) throws Exception {
        List<RepaymentSchedule> schedules = repaymentScheduleRepository.findBySmeLoanId(loanId);
        if (schedules.isEmpty()) {
            throw new RuntimeException("No repayment schedule found for loan ID: " + loanId);
        }

        InputStream templateStream = getClass().getResourceAsStream("/reports/repayment_schedule.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(schedules);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("loanId", loanId);

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        if (format.equalsIgnoreCase("pdf")) {
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } else if (format.equalsIgnoreCase("excel")) {
            return exportToExcel(jasperPrint); // ✅ Now includes improved formatting
        }
        return null;
    }

    private byte[] exportToExcel(JasperPrint jasperPrint) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JRXlsxExporter exporter = new JRXlsxExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));


        SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
        configuration.setOnePagePerSheet(false);
        configuration.setDetectCellType(true);
        configuration.setWhitePageBackground(false);
        configuration.setRemoveEmptySpaceBetweenRows(true);
        configuration.setCollapseRowSpan(false);
        configuration.setShowGridLines(true); // ✅ Show grid lines
        configuration.setAutoFitPageHeight(true); // ✅ Auto-fit row heights
        configuration.setIgnoreGraphics(false); // ✅ Ensure borders appear

        exporter.setConfiguration(configuration);
        exporter.exportReport();

        return outputStream.toByteArray();
    }
}

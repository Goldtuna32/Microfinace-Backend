package com.sme.service.impl;

import com.sme.dto.CIFDTO;
import com.sme.service.CIFService;
import com.sme.service.ReportService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private CIFService cifService;

    @Autowired
    private DataSource dataSource;

    @Override
    public byte[] generateActiveCIFReport(String format) throws Exception {
        return generateReport("active", "/reports/cif_active_report.jrxml", cifService.getAllCifs(), format);
    }

    @Override
    public byte[] generateDeletedCIFReport(String format) throws Exception {
        return generateReport("deleted", "/reports/cif_deleted_report.jrxml", cifService.getDeletedCIFs(), format);
    }

    private byte[] generateReport(String reportType, String jrxmlPath, List<CIFDTO> cifList, String format) throws Exception {
        // Prepare JRBeanCollectionDataSource
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(cifList);

        // Load JRXML file
        InputStream reportStream = this.getClass().getResourceAsStream(jrxmlPath);
        if (reportStream == null) {
            throw new Exception("Cannot find JRXML file at: " + jrxmlPath);
        }
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        // Parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("REPORT_TITLE", reportType.equals("active") ? "Active CIF" : "Deleted CIF");
        parameters.put("CREATED_DATE", LocalDateTime.now());
        parameters.put("ITEMS_PER_PAGE", 10);

        // Fill report
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport,
                parameters,
                dataSource
        );

        // Export based on format
        if ("excel".equalsIgnoreCase(format)) {
            return exportToExcel(jasperPrint);
        } else {
            return JasperExportManager.exportReportToPdf(jasperPrint);
        }
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
        configuration.setShowGridLines(true);
        configuration.setAutoFitPageHeight(true);
        configuration.setIgnoreGraphics(false);
        configuration.setForcePageBreaks(true);

        exporter.setConfiguration(configuration);
        exporter.exportReport();

        return outputStream.toByteArray();
    }
}

package com.sme.service.impl;

import com.sme.dto.AccountTransactionDTO;
import com.sme.dto.CIFDTO;
import com.sme.dto.CollateralDTO;
import com.sme.dto.CurrentAccountDTO;
import com.sme.entity.AccountTransaction;
import com.sme.repository.AccountTransactionRepository;
import com.sme.service.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private CIFService cifService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CurrentAccountService currentAccountService;

    @Autowired
    private CollateralService collateralService;

    @Autowired
    private AccountTransactionService accountTransactionService;

    @Autowired
    private AccountTransactionRepository transactionRepository;


    @Override
    public byte[] generateActiveCIFReport(String format) throws Exception {
        return generateReport("active", "/reports/cif_active_report.jrxml", cifService.getAllCifs(), format);
    }

    @Override
    public byte[] generateDeletedCIFReport(String format) throws Exception {
        return generateReport("deleted", "/reports/cif_deleted_report.jrxml", cifService.getDeletedCIFS(), format);
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

    @Override
    public byte[] generateCIFDetailReport(Long cifId, String format) throws Exception {
        System.out.println("Starting report generation for CIF ID: " + cifId);
        long startTime = System.currentTimeMillis();

        Optional<CIFDTO> cifOptional = cifService.getCIFById(cifId);
        if (!cifOptional.isPresent()) {
            throw new Exception("CIF not found with ID: " + cifId);
        }
        CIFDTO cif = cifOptional.get();
        System.out.println("CIF fetched: " + cif.getSerialNumber());

        // Fetch related data
        CurrentAccountDTO currentAccount = cif.isHasCurrentAccount() ?
                currentAccountService.getCurrentAccountByCifId(cifId) : null;
        System.out.println("Current account: " + (currentAccount != null ? currentAccount.getAccountNumber() : "N/A"));

        // Fetch detailed transactions (for the table)
        List<AccountTransactionDTO> transactions = currentAccount != null ?
                new ArrayList<>(new LinkedHashSet<>(accountTransactionService.getTransactionsByCurrentAccount(currentAccount.getId()))) : Collections.emptyList();
        System.out.println("Transactions fetched: " + transactions.size());

        // Summarize transactions for the chart
        List<Map<String, Object>> summarizedTransactions = new ArrayList<>();
        if (!transactions.isEmpty()) {
            Map<String, BigDecimal> summaryMap = transactions.stream()
                    .collect(Collectors.groupingBy(
                            AccountTransactionDTO::getTransactionType,
                            Collectors.reducing(
                                    BigDecimal.ZERO,
                                    AccountTransactionDTO::getAmount,
                                    BigDecimal::add
                            )
                    ));
            summaryMap.forEach((type, totalAmount) -> {
                Map<String, Object> summary = new HashMap<>();
                summary.put("transactionType", type);
                summary.put("amount", totalAmount);
                summarizedTransactions.add(summary);
            });
        }
        System.out.println("Summarized transactions: " + summarizedTransactions.size());

        List<CollateralDTO> collaterals = new ArrayList<>(new LinkedHashSet<>(collateralService.getCollateralsByCifId(cifId)));
        System.out.println("Collaterals fetched: " + collaterals.size());

        // Prepare data map
        Map<String, Object> cifData = new HashMap<>();
        cifData.put("name", cif.getName());
        cifData.put("nrcNumber", cif.getNrcNumber());
        cifData.put("dob", cif.getDob());
        cifData.put("gender", cif.getGender());
        cifData.put("phoneNumber", cif.getPhoneNumber());
        cifData.put("email", cif.getEmail());
        cifData.put("address", cif.getAddress());
        cifData.put("maritalStatus", cif.getMaritalStatus());
        cifData.put("occupation", cif.getOccupation());
        cifData.put("incomeSource", cif.getIncomeSource());
        cifData.put("serialNumber", cif.getSerialNumber());
        cifData.put("createdAt", cif.getCreatedAt());
        cifData.put("branchId", cif.getBranchId());
        cifData.put("hasCurrentAccount", cif.isHasCurrentAccount());
        cifData.put("fNrcPhotoUrl", cif.getFNrcPhotoUrl());
        cifData.put("bNrcPhotoUrl", cif.getBNrcPhotoUrl());
        cifData.put("currentAccount", currentAccount);
        cifData.put("transactions", transactions);
        cifData.put("summarizedTransactions", summarizedTransactions);
        cifData.put("collaterals", collaterals);

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(cifData));
        System.out.println("Data source prepared");

        // Compile main report
        InputStream mainReportStream = this.getClass().getResourceAsStream("/reports/cif_detail_report.jrxml");
        if (mainReportStream == null) {
            throw new Exception("Cannot find JRXML file at: /reports/cif_detail_report.jrxml");
        }
        JasperReport jasperReport = JasperCompileManager.compileReport(mainReportStream);
        System.out.println("Main report compiled");

        // Compile subreports dynamically
        JasperReport collateralSubreport = compileSubreport("/reports/collateral_subreport.jrxml");
        JasperReport transactionSubreport = compileSubreport("/reports/transaction_subreport.jrxml");

        // Pass compiled subreports and lists as parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("REPORT_TITLE", "CIF Detail Report - " + cif.getSerialNumber());
        parameters.put("CREATED_DATE", LocalDateTime.now());
        parameters.put("TRANSACTION_LIST", transactions); // Set TRANSACTION_LIST
        parameters.put("COLLATERAL_LIST", collaterals);   // Set COLLATERAL_LIST

        if (collateralSubreport != null) {
            parameters.put("COLLATERAL_SUBREPORT", collateralSubreport);
            System.out.println("Collateral Subreport: " + collateralSubreport);
        } else {
            System.out.println("Collateral Subreport is null");
        }
        if (transactionSubreport != null) {
            parameters.put("TRANSACTION_SUBREPORT", transactionSubreport);
            System.out.println("Transaction Subreport: " + transactionSubreport);
        } else {
            System.out.println("Transaction Subreport is null");
        }

        System.out.println("Transactions: " + transactions);
        System.out.println("Collaterals: " + collaterals);

        // Fill the report
        System.out.println("Filling report...");
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        System.out.println("Report filled");

        // Export based on format
        System.out.println("Exporting report to " + format);
        byte[] reportBytes;
        if ("excel".equalsIgnoreCase(format)) {
            reportBytes = exportToExcel(jasperPrint);
        } else {
            reportBytes = JasperExportManager.exportReportToPdf(jasperPrint);
        }
        System.out.println("Report exported, size: " + reportBytes.length + " bytes");
        System.out.println("Time taken: " + (System.currentTimeMillis() - startTime) + "ms");

        return reportBytes;
    }

    private JasperReport compileSubreport(String jrxmlPath) throws Exception {
        InputStream subreportStream = this.getClass().getResourceAsStream(jrxmlPath);
        if (subreportStream == null) {
            System.out.println("Subreport JRXML not found at: " + jrxmlPath);
            return null;
        }
        try {
            return JasperCompileManager.compileReport(subreportStream);
        } catch (Exception e) {
            System.err.println("Failed to compile subreport at: " + jrxmlPath + " - " + e.getMessage());
            throw e;
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


    private List<AccountTransactionDTO> getTransactionData() {
        // Fetch transactions sorted by transactionDate
        List<AccountTransaction> transactions = transactionRepository.findAll(Sort.by(Sort.Direction.ASC, "transactionDate"));
        return transactions.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private AccountTransactionDTO mapToDTO(AccountTransaction entity) {
        AccountTransactionDTO dto = new AccountTransactionDTO();
        dto.setId((long) entity.getId());
        dto.setTransactionType(entity.getTransactionType());
        dto.setAmount(new BigDecimal(entity.getAmount()));
        dto.setTransactionDescription(entity.getTransactionDescription());
        dto.setStatus(entity.getStatus());
        dto.setTransactionDate(entity.getTransactionDate());
        dto.setCurrentAccountId(entity.getCurrentAccount() != null ? entity.getCurrentAccount().getId() : null);
        return dto;
    }

    @Override
    public byte[] generateTransactionReport(String format) throws Exception {
        InputStream reportStream = getClass().getResourceAsStream("/reports/transaction_report.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        List<AccountTransactionDTO> transactions = getTransactionData();
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(transactions);

        Map<String, Object> parameters = new HashMap<>();
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if ("pdf".equalsIgnoreCase(format)) {
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
        } else if ("excel".equalsIgnoreCase(format)) {
            JRXlsExporter exporter = new JRXlsExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.exportReport();
        } else {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }

        return outputStream.toByteArray();
    }
}

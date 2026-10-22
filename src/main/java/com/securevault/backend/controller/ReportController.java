package com.securevault.backend.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.securevault.backend.dto.ReportResponse;
import com.securevault.backend.service.ReportExportService;
import com.securevault.backend.service.ReportService;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin
public class ReportController {

    private final ReportService reportService;
    private final ReportExportService reportExportService;

    public ReportController(
            ReportService reportService,
            ReportExportService reportExportService) {

        this.reportService = reportService;
        this.reportExportService = reportExportService;
    }

    // =========================================================
    // SECURITY REPORT
    // =========================================================

    @GetMapping("/security")
    public ResponseEntity<ReportResponse> securityReport(
            Authentication authentication) {

        return ResponseEntity.ok(
                reportService.generateSecurityReport(
                        authentication.getName()
                )
        );
    }

    // =========================================================
    // PASSWORD HEALTH
    // =========================================================

    @GetMapping("/password-health")
    public ResponseEntity<ReportResponse> passwordHealthReport(
            Authentication authentication) {

        return ResponseEntity.ok(
                reportService.generatePasswordHealthReport(
                        authentication.getName()
                )
        );
    }

    // =========================================================
    // USER ACTIVITY
    // =========================================================

    @GetMapping("/user-activity")
    public ResponseEntity<ReportResponse> userActivityReport(
            Authentication authentication) {

        return ResponseEntity.ok(
                reportService.generateUserActivityReport(
                        authentication.getName()
                )
        );
    }

    // =========================================================
    // THREAT MONITORING
    // =========================================================

    @GetMapping("/threat-monitoring")
    public ResponseEntity<ReportResponse> threatMonitoringReport(
            Authentication authentication) {

        return ResponseEntity.ok(
                reportService.generateThreatMonitoringReport(
                        authentication.getName()
                )
        );
    }

    // =========================================================
    // SECURITY PDF
    // =========================================================

    @GetMapping("/security/pdf")
    public ResponseEntity<byte[]> securityPdf(
            Authentication authentication)
            throws Exception {

        byte[] pdf =
                reportExportService
                        .generateSecurityReportPdf(
                                authentication.getName()
                        );

        return pdfResponse(
                pdf,
                "security-report.pdf"
        );
    }

    // =========================================================
    // SECURITY EXCEL
    // =========================================================

    @GetMapping("/security/excel")
    public ResponseEntity<byte[]> securityExcel(
            Authentication authentication)
            throws Exception {

        byte[] excel =
                reportExportService
                        .generateSecurityReportExcel(
                                authentication.getName()
                        );

        return excelResponse(
                excel,
                "security-report.xlsx"
        );
    }

    // =========================================================
    // PASSWORD HEALTH PDF
    // =========================================================

    @GetMapping("/password-health/pdf")
    public ResponseEntity<byte[]> passwordHealthPdf(
            Authentication authentication)
            throws Exception {

        byte[] pdf =
                reportExportService
                        .generatePasswordHealthReportPdf(
                                authentication.getName()
                        );

        return pdfResponse(
                pdf,
                "password-health-report.pdf"
        );
    }

    // =========================================================
    // PASSWORD HEALTH EXCEL
    // =========================================================

    @GetMapping("/password-health/excel")
    public ResponseEntity<byte[]> passwordHealthExcel(
            Authentication authentication)
            throws Exception {

        byte[] excel =
                reportExportService
                        .generatePasswordHealthReportExcel(
                                authentication.getName()
                        );

        return excelResponse(
                excel,
                "password-health-report.xlsx"
        );
    }

    // =========================================================
    // USER ACTIVITY PDF
    // =========================================================

    @GetMapping("/user-activity/pdf")
    public ResponseEntity<byte[]> userActivityPdf(
            Authentication authentication)
            throws Exception {

        byte[] pdf =
                reportExportService
                        .generateUserActivityReportPdf(
                                authentication.getName()
                        );

        return pdfResponse(
                pdf,
                "user-activity-report.pdf"
        );
    }

    // =========================================================
    // USER ACTIVITY EXCEL
    // =========================================================

    @GetMapping("/user-activity/excel")
    public ResponseEntity<byte[]> userActivityExcel(
            Authentication authentication)
            throws Exception {

        byte[] excel =
                reportExportService
                        .generateUserActivityReportExcel(
                                authentication.getName()
                        );

        return excelResponse(
                excel,
                "user-activity-report.xlsx"
        );
    }

    // =========================================================
    // THREAT MONITORING PDF
    // =========================================================

    @GetMapping("/threat-monitoring/pdf")
    public ResponseEntity<byte[]> threatMonitoringPdf(
            Authentication authentication)
            throws Exception {

        byte[] pdf =
                reportExportService
                        .generateThreatMonitoringReportPdf(
                                authentication.getName()
                        );

        return pdfResponse(
                pdf,
                "threat-monitoring-report.pdf"
        );
    }

    // =========================================================
    // THREAT MONITORING EXCEL
    // =========================================================

    @GetMapping("/threat-monitoring/excel")
    public ResponseEntity<byte[]> threatMonitoringExcel(
            Authentication authentication)
            throws Exception {

        byte[] excel =
                reportExportService
                        .generateThreatMonitoringReportExcel(
                                authentication.getName()
                        );

        return excelResponse(
                excel,
                "threat-monitoring-report.xlsx"
        );
    }

    // =========================================================
    // PDF RESPONSE
    // =========================================================

    private ResponseEntity<byte[]> pdfResponse(
            byte[] data,
            String filename) {

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_PDF
        );

        headers.setContentDisposition(
                ContentDisposition
                        .attachment()
                        .filename(filename)
                        .build()
        );

        headers.setContentLength(
                data.length
        );

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(data);
    }

    // =========================================================
    // EXCEL RESPONSE
    // =========================================================

    private ResponseEntity<byte[]> excelResponse(
            byte[] data,
            String filename) {

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
        );

        headers.setContentDisposition(
                ContentDisposition
                        .attachment()
                        .filename(filename)
                        .build()
        );

        headers.setContentLength(
                data.length
        );

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(data);
    }
}
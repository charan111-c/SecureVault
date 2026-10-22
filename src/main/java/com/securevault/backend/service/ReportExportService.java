package com.securevault.backend.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfWriter;
import com.securevault.backend.entity.Credential;
import com.securevault.backend.entity.Notification;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.CredentialRepository;
import com.securevault.backend.repository.NotificationRepository;
import com.securevault.backend.repository.UserRepository;

@Service
public class ReportExportService {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final NotificationRepository notificationRepository;

    public ReportExportService(
            UserRepository userRepository,
            CredentialRepository credentialRepository,
            NotificationRepository notificationRepository) {

        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.notificationRepository = notificationRepository;
    }

    // =========================================================
    // SECURITY REPORT - PDF
    // =========================================================

    public byte[] generateSecurityReportPdf(String email)
            throws IOException, DocumentException {

        User user = getUser(email);

        List<Credential> credentials =
                credentialRepository.findByUser(user);

        List<Notification> notifications =
                notificationRepository
                        .findByUserOrderByCreatedAtDesc(user);

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        Document document = new Document();

        PdfWriter.getInstance(
                document,
                outputStream
        );

        document.open();

        document.add(
                new Paragraph("SecureVault Security Report")
        );

        document.add(
                new Paragraph(
                        "User: " + user.getEmail()
                )
        );

        document.add(
                new Paragraph(
                        "Total Credentials: "
                                + credentials.size()
                )
        );

        document.add(
                new Paragraph(
                        "Total Notifications: "
                                + notifications.size()
                )
        );

        document.add(
                new Paragraph(" ")
        );

        document.add(
                new Paragraph("Security Notifications")
        );

        Table table =
                new Table(4);

        table.addCell("Title");
        table.addCell("Type");
        table.addCell("Read");
        table.addCell("Created At");

        for (Notification notification : notifications) {

            table.addCell(
                    safe(notification.getTitle())
            );

            table.addCell(
                    notification.getNotificationType() != null
                            ? notification
                                    .getNotificationType()
                                    .name()
                            : "UNKNOWN"
            );

            table.addCell(
                    notification.isRead()
                            ? "Yes"
                            : "No"
            );

            table.addCell(
                    notification.getCreatedAt() != null
                            ? notification
                                    .getCreatedAt()
                                    .toString()
                            : ""
            );
        }

        document.add(table);

        document.close();

        return outputStream.toByteArray();
    }

    // =========================================================
    // SECURITY REPORT - EXCEL
    // =========================================================

    public byte[] generateSecurityReportExcel(String email)
            throws IOException {

        User user = getUser(email);

        List<Credential> credentials =
                credentialRepository.findByUser(user);

        List<Notification> notifications =
                notificationRepository
                        .findByUserOrderByCreatedAtDesc(user);

        try (Workbook workbook =
                     new XSSFWorkbook()) {

            Sheet sheet =
                    workbook.createSheet(
                            "Security Report"
                    );

            int rowNumber = 0;

            Row titleRow =
                    sheet.createRow(rowNumber++);

            titleRow.createCell(0)
                    .setCellValue(
                            "SecureVault Security Report"
                    );

            Row userRow =
                    sheet.createRow(rowNumber++);

            userRow.createCell(0)
                    .setCellValue("User");

            userRow.createCell(1)
                    .setCellValue(user.getEmail());

            Row credentialRow =
                    sheet.createRow(rowNumber++);

            credentialRow.createCell(0)
                    .setCellValue(
                            "Total Credentials"
                    );

            credentialRow.createCell(1)
                    .setCellValue(
                            credentials.size()
                    );

            Row notificationRow =
                    sheet.createRow(rowNumber++);

            notificationRow.createCell(0)
                    .setCellValue(
                            "Total Notifications"
                    );

            notificationRow.createCell(1)
                    .setCellValue(
                            notifications.size()
                    );

            rowNumber++;

            Row header =
                    sheet.createRow(rowNumber++);

            header.createCell(0)
                    .setCellValue("Title");

            header.createCell(1)
                    .setCellValue("Type");

            header.createCell(2)
                    .setCellValue("Read");

            header.createCell(3)
                    .setCellValue("Created At");

            for (Notification notification :
                    notifications) {

                Row row =
                        sheet.createRow(rowNumber++);

                row.createCell(0)
                        .setCellValue(
                                safe(notification.getTitle())
                        );

                row.createCell(1)
                        .setCellValue(
                                notification
                                        .getNotificationType() != null
                                        ? notification
                                            .getNotificationType()
                                            .name()
                                        : "UNKNOWN"
                        );

                row.createCell(2)
                        .setCellValue(
                                notification.isRead()
                                        ? "Yes"
                                        : "No"
                        );

                row.createCell(3)
                        .setCellValue(
                                notification.getCreatedAt() != null
                                        ? notification
                                            .getCreatedAt()
                                            .toString()
                                        : ""
                        );
            }

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            workbook.write(outputStream);

            return outputStream.toByteArray();
        }
    }

    // =========================================================
    // PASSWORD HEALTH REPORT - PDF
    // =========================================================

    public byte[] generatePasswordHealthReportPdf(
            String email)
            throws IOException, DocumentException {

        User user = getUser(email);

        List<Credential> credentials =
                credentialRepository.findByUser(user);

        long total =
                credentials.size();

        long emptyPasswords =
                credentials.stream()
                        .filter(c ->
                                c.getPassword() == null
                                        || c.getPassword().isBlank())
                        .count();

        long healthy =
                total - emptyPasswords;

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        Document document =
                new Document();

        PdfWriter.getInstance(
                document,
                outputStream
        );

        document.open();

        document.add(
                new Paragraph(
                        "SecureVault Password Health Report"
                )
        );

        document.add(
                new Paragraph(
                        "User: " + user.getEmail()
                )
        );

        document.add(
                new Paragraph(
                        "Total Credentials: " + total
                )
        );

        document.add(
                new Paragraph(
                        "Credentials With Password: "
                                + healthy
                )
        );

        document.add(
                new Paragraph(
                        "Credentials Without Password: "
                                + emptyPasswords
                )
        );

        document.close();

        return outputStream.toByteArray();
    }

    // =========================================================
    // PASSWORD HEALTH REPORT - EXCEL
    // =========================================================

    public byte[] generatePasswordHealthReportExcel(
            String email)
            throws IOException {

        User user = getUser(email);

        List<Credential> credentials =
                credentialRepository.findByUser(user);

        long total =
                credentials.size();

        long emptyPasswords =
                credentials.stream()
                        .filter(c ->
                                c.getPassword() == null
                                        || c.getPassword().isBlank())
                        .count();

        long healthy =
                total - emptyPasswords;

        try (Workbook workbook =
                     new XSSFWorkbook()) {

            Sheet sheet =
                    workbook.createSheet(
                            "Password Health"
                    );

            Row header =
                    sheet.createRow(0);

            header.createCell(0)
                    .setCellValue("Metric");

            header.createCell(1)
                    .setCellValue("Value");

            Row row1 =
                    sheet.createRow(1);

            row1.createCell(0)
                    .setCellValue(
                            "Total Credentials"
                    );

            row1.createCell(1)
                    .setCellValue(total);

            Row row2 =
                    sheet.createRow(2);

            row2.createCell(0)
                    .setCellValue(
                            "Credentials With Password"
                    );

            row2.createCell(1)
                    .setCellValue(healthy);

            Row row3 =
                    sheet.createRow(3);

            row3.createCell(0)
                    .setCellValue(
                            "Credentials Without Password"
                    );

            row3.createCell(1)
                    .setCellValue(emptyPasswords);

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            workbook.write(outputStream);

            return outputStream.toByteArray();
        }
    }

    // =========================================================
    // USER ACTIVITY - PDF
    // =========================================================

    public byte[] generateUserActivityReportPdf(
            String email)
            throws IOException, DocumentException {

        User user = getUser(email);

        List<Notification> notifications =
                notificationRepository
                        .findByUserOrderByCreatedAtDesc(user);

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        Document document =
                new Document();

        PdfWriter.getInstance(
                document,
                outputStream
        );

        document.open();

        document.add(
                new Paragraph(
                        "SecureVault User Activity Report"
                )
        );

        document.add(
                new Paragraph(
                        "User: " + user.getEmail()
                )
        );

        document.add(
                new Paragraph(
                        "Total Recorded Activities: "
                                + notifications.size()
                )
        );

        document.add(
                new Paragraph(" ")
        );

        for (Notification notification :
                notifications) {

            String activity =
                    notification.getTitle()
                            + " - "
                            + notification.getMessage();

            document.add(
                    new Paragraph(
                            safe(activity)
                    )
            );
        }

        document.close();

        return outputStream.toByteArray();
    }

    // =========================================================
    // USER ACTIVITY - EXCEL
    // =========================================================

    public byte[] generateUserActivityReportExcel(
            String email)
            throws IOException {

        User user = getUser(email);

        List<Notification> notifications =
                notificationRepository
                        .findByUserOrderByCreatedAtDesc(user);

        try (Workbook workbook =
                     new XSSFWorkbook()) {

            Sheet sheet =
                    workbook.createSheet(
                            "User Activity"
                    );

            Row header =
                    sheet.createRow(0);

            header.createCell(0)
                    .setCellValue("Title");

            header.createCell(1)
                    .setCellValue("Message");

            header.createCell(2)
                    .setCellValue("Type");

            header.createCell(3)
                    .setCellValue("Created At");

            int rowNumber = 1;

            for (Notification notification :
                    notifications) {

                Row row =
                        sheet.createRow(
                                rowNumber++
                        );

                row.createCell(0)
                        .setCellValue(
                                safe(notification.getTitle())
                        );

                row.createCell(1)
                        .setCellValue(
                                safe(notification.getMessage())
                        );

                row.createCell(2)
                        .setCellValue(
                                notification
                                        .getNotificationType() != null
                                        ? notification
                                            .getNotificationType()
                                            .name()
                                        : "UNKNOWN"
                        );

                row.createCell(3)
                        .setCellValue(
                                notification.getCreatedAt() != null
                                        ? notification
                                            .getCreatedAt()
                                            .toString()
                                        : ""
                        );
            }

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            workbook.write(outputStream);

            return outputStream.toByteArray();
        }
    }

    // =========================================================
    // THREAT MONITORING - PDF
    // =========================================================

    public byte[] generateThreatMonitoringReportPdf(
            String email)
            throws IOException, DocumentException {

        User user = getUser(email);

        List<Notification> notifications =
                notificationRepository
                        .findByUserOrderByCreatedAtDesc(user);

        long threats =
                notifications.stream()
                        .filter(n ->
                                n.getNotificationType() != null
                                        && n.getNotificationType()
                                        .name()
                                        .toLowerCase()
                                        .contains("risk"))
                        .count();

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        Document document =
                new Document();

        PdfWriter.getInstance(
                document,
                outputStream
        );

        document.open();

        document.add(
                new Paragraph(
                        "SecureVault Threat Monitoring Report"
                )
        );

        document.add(
                new Paragraph(
                        "User: " + user.getEmail()
                )
        );

        document.add(
                new Paragraph(
                        "Total Notifications: "
                                + notifications.size()
                )
        );

        document.add(
                new Paragraph(
                        "Potential Risk Alerts: "
                                + threats
                )
        );

        document.close();

        return outputStream.toByteArray();
    }

    // =========================================================
    // THREAT MONITORING - EXCEL
    // =========================================================

    public byte[] generateThreatMonitoringReportExcel(
            String email)
            throws IOException {

        User user = getUser(email);

        List<Notification> notifications =
                notificationRepository
                        .findByUserOrderByCreatedAtDesc(user);

        try (Workbook workbook =
                     new XSSFWorkbook()) {

            Sheet sheet =
                    workbook.createSheet(
                            "Threat Monitoring"
                    );

            Row header =
                    sheet.createRow(0);

            header.createCell(0)
                    .setCellValue("Title");

            header.createCell(1)
                    .setCellValue("Type");

            header.createCell(2)
                    .setCellValue("Message");

            header.createCell(3)
                    .setCellValue("Created At");

            int rowNumber = 1;

            for (Notification notification :
                    notifications) {

                Row row =
                        sheet.createRow(
                                rowNumber++
                        );

                row.createCell(0)
                        .setCellValue(
                                safe(notification.getTitle())
                        );

                row.createCell(1)
                        .setCellValue(
                                notification
                                        .getNotificationType() != null
                                        ? notification
                                            .getNotificationType()
                                            .name()
                                        : "UNKNOWN"
                        );

                row.createCell(2)
                        .setCellValue(
                                safe(notification.getMessage())
                        );

                row.createCell(3)
                        .setCellValue(
                                notification.getCreatedAt() != null
                                        ? notification
                                            .getCreatedAt()
                                            .toString()
                                        : ""
                        );
            }

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            workbook.write(outputStream);

            return outputStream.toByteArray();
        }
    }

    // =========================================================
    // FIND USER
    // =========================================================

    private User getUser(String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        ));
    }

    // =========================================================
    // SAFE TEXT
    // =========================================================

    private String safe(String value) {

        if (value == null) {
            return "";
        }

        return value;
    }
}
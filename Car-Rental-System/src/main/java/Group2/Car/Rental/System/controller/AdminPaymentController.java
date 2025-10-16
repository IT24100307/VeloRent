package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.PaymentAdminDTO;
import Group2.Car.Rental.System.service.PaymentAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

@RestController
@RequestMapping("/api/admin/payments")
//@PreAuthorize("hasAnyRole('ROLE_OWNER','ROLE_SYSTEM_ADMIN')")
public class AdminPaymentController {

    @Autowired
    private PaymentAdminService paymentAdminService;

    @GetMapping
    public ResponseEntity<List<PaymentAdminDTO>> list() {
        return ResponseEntity.ok(paymentAdminService.getAllPayments());
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> summary() {
        return ResponseEntity.ok(paymentAdminService.getSummary());
    }

    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<Map<String, Object>> confirm(@PathVariable Integer paymentId) {
        Map<String, Object> res = paymentAdminService.confirmPayment(paymentId);
        return Boolean.TRUE.equals(res.get("success")) ? ResponseEntity.ok(res) : ResponseEntity.badRequest().body(res);
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<Map<String, Object>> cancel(@PathVariable Integer paymentId) {
        Map<String, Object> res = paymentAdminService.cancelPayment(paymentId);
        return Boolean.TRUE.equals(res.get("success")) ? ResponseEntity.ok(res) : ResponseEntity.badRequest().body(res);
    }

    @DeleteMapping("/{paymentId}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Integer paymentId) {
        Map<String, Object> res = paymentAdminService.deletePayment(paymentId);
        return Boolean.TRUE.equals(res.get("success")) ? ResponseEntity.ok(res) : ResponseEntity.badRequest().body(res);
    }

    // New: Export payments as Excel (without Txn column)
    @GetMapping("/export")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        var summary = paymentAdminService.getSummary();
        var payments = paymentAdminService.getAllPayments();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Payments");

            // Styles
            CellStyle titleStyle = workbook.createCellStyle();
            var titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            CellStyle headerStyle = workbook.createCellStyle();
            var headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle moneyStyle = workbook.createCellStyle();
            DataFormat df = workbook.createDataFormat();
            moneyStyle.setDataFormat(df.getFormat("$#,##0.00"));

            int rowIdx = 0;
            // Title
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("VeloRent Payments Report");
            titleCell.setCellStyle(titleStyle);

            Row dateRow = sheet.createRow(rowIdx++);
            dateRow.createCell(0).setCellValue("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

            rowIdx++; // blank

            // Summary section
            Row sumHeader = sheet.createRow(rowIdx++);
            Cell sh = sumHeader.createCell(0);
            sh.setCellValue("Summary");
            sh.setCellStyle(headerStyle);

            Row tr = sheet.createRow(rowIdx++);
            tr.createCell(0).setCellValue("Total Revenue");
            Cell trVal = tr.createCell(1);
            Object totalRevenue = summary.getOrDefault("totalRevenue", 0);
            try { trVal.setCellValue(Double.parseDouble(totalRevenue.toString())); trVal.setCellStyle(moneyStyle);} catch(Exception e){ trVal.setCellValue(totalRevenue.toString()); }

            Row tp = sheet.createRow(rowIdx++);
            tp.createCell(0).setCellValue("Total Payments");
            tp.createCell(1).setCellValue(Double.parseDouble(String.valueOf(summary.getOrDefault("totalPayments", 0))));

            Row lp = sheet.createRow(rowIdx++);
            lp.createCell(0).setCellValue("Last Payment");
            lp.createCell(1).setCellValue(String.valueOf(summary.getOrDefault("lastPaymentDate", "-")));

            rowIdx++; // blank

            // Table header (removed Txn)
            Row header = sheet.createRow(rowIdx++);
            String[] cols = new String[]{"ID","Date","Booking","Customer","Email","Method","Amount","Status"};
            for (int i=0;i<cols.length;i++){
                Cell c = header.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(headerStyle);
            }
            int headerRowNum = header.getRowNum();

            // Rows (removed transactionId)
            for (PaymentAdminDTO p : payments) {
                Row r = sheet.createRow(rowIdx++);
                int col = 0;
                r.createCell(col++).setCellValue(p.getPaymentId());
                r.createCell(col++).setCellValue(p.getPaymentDate() != null ? p.getPaymentDate().toString() : "");
                r.createCell(col++).setCellValue("#" + p.getBookingId());
                r.createCell(col++).setCellValue(p.getCustomerName());
                r.createCell(col++).setCellValue(p.getCustomerEmail());
                r.createCell(col++).setCellValue(p.getPaymentMethod());
                Cell amountCell = r.createCell(col++);
                if (p.getAmount() != null) {
                    try { amountCell.setCellValue(p.getAmount().doubleValue()); amountCell.setCellStyle(moneyStyle);} catch(Exception e){ amountCell.setCellValue(p.getAmount().toString()); }
                } else {
                    amountCell.setCellValue(0);
                }
                r.createCell(col++).setCellValue(p.getPaymentStatus());
            }

            // Autosize (8 columns)
            for (int i=0;i<8;i++){ sheet.autoSizeColumn(i); }

            // Print setup to fit to page width
            PrintSetup ps = sheet.getPrintSetup();
            ps.setLandscape(true);
            ps.setPaperSize(PrintSetup.A4_PAPERSIZE);
            sheet.setAutobreaks(true);
            sheet.setFitToPage(true);
            ps.setFitWidth((short)1);
            ps.setFitHeight((short)0);
            sheet.setMargin(Sheet.LeftMargin, 0.25);
            sheet.setMargin(Sheet.RightMargin, 0.25);
            sheet.setMargin(Sheet.TopMargin, 0.5);
            sheet.setMargin(Sheet.BottomMargin, 0.5);
            sheet.setRepeatingRows(new CellRangeAddress(headerRowNum, headerRowNum, 0, cols.length-1));

            // Prepare response
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String filename = "velorent-payments-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")) + ".xlsx";
            String cd = "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+","%20") + "\"";
            response.setHeader("Content-Disposition", cd);
            workbook.write(response.getOutputStream());
            response.flushBuffer();
        }
    }
}

package project;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class VaccineStockReportGenerator {
    private final Connection conn;

    public VaccineStockReportGenerator(Connection conn) {
        this.conn = conn;
    }

    public String generateReport() throws Exception {
        String fileName = "Vaccine_Stock_Report_" + LocalDate.now() + ".pdf";
        PdfWriter writer = new PdfWriter(fileName);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        DeviceRgb blue = new DeviceRgb(0, 102, 204);
        DeviceRgb green = new DeviceRgb(0, 170, 0);
        DeviceRgb red = new DeviceRgb(220, 53, 69);
        DeviceRgb orange = new DeviceRgb(255, 140, 0);

        // Title
        doc.add(new Paragraph("💉 VACCINE STOCK REPORT")
                .setFontSize(26).setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(blue)
                .setMarginBottom(10));

        doc.add(new Paragraph("Government of Nepal - Child Vaccination System")
                .setFontSize(12).setItalic()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        doc.add(new Paragraph("Report Date: " + LocalDate.now())
                .setFontSize(11)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(20));

        // Summary Statistics
        doc.add(new Paragraph("📊 STOCK SUMMARY")
                .setFontSize(18).setBold()
                .setFontColor(blue)
                .setMarginTop(10)
                .setMarginBottom(15));

        Table summaryTable = new Table(4).useAllAvailableWidth();

        PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) as total, SUM(Quantity) as total_qty, " +
                        "SUM(CASE WHEN Quantity < 50 THEN 1 ELSE 0 END) as low_stock, " +
                        "SUM(CASE WHEN Expiry_Date < CURDATE() THEN 1 ELSE 0 END) as expired " +
                        "FROM vaccine_stock"
        );
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            summaryTable.addCell(createSummaryCell("Total Batches", rs.getInt("total") + "", blue));
            summaryTable.addCell(createSummaryCell("Total Quantity", rs.getInt("total_qty") + "", green));
            summaryTable.addCell(createSummaryCell("Low Stock", rs.getInt("low_stock") + "", orange));
            summaryTable.addCell(createSummaryCell("Expired", rs.getInt("expired") + "", red));
        }
        doc.add(summaryTable);

        // Main Stock Table
        doc.add(new Paragraph("📦 DETAILED STOCK INVENTORY")
                .setFontSize(18).setBold()
                .setFontColor(blue)
                .setMarginTop(25)
                .setMarginBottom(15));

        Table stockTable = new Table(7).useAllAvailableWidth();

        String[] headers = {"Vaccine", "Batch", "Quantity", "Expiry Date", "Days Left", "Status", "Supplier"};
        for (String header : headers) {
            stockTable.addHeaderCell(new Cell()
                    .add(new Paragraph(header).setBold().setFontColor(DeviceRgb.WHITE))
                    .setBackgroundColor(blue)
                    .setPadding(10)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        PreparedStatement ps2 = conn.prepareStatement(
                "SELECT * FROM vaccine_stock ORDER BY Vaccine_Name, Expiry_Date"
        );
        ResultSet rs2 = ps2.executeQuery();

        while (rs2.next()) {
            String vaccine = rs2.getString("Vaccine_Name");
            String batch = rs2.getString("Batch_Number");
            int quantity = rs2.getInt("Quantity");
            LocalDate expiry = rs2.getDate("Expiry_Date").toLocalDate();
            String supplier = rs2.getString("Supplier");

            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), expiry);
            String status = getStatus(quantity, daysLeft);
            DeviceRgb statusColor = getStatusColor(quantity, daysLeft);

            stockTable.addCell(createCell(vaccine));
            stockTable.addCell(createCell(batch));
            stockTable.addCell(createCell(String.valueOf(quantity)).setTextAlignment(TextAlignment.CENTER));
            stockTable.addCell(createCell(expiry.toString()).setTextAlignment(TextAlignment.CENTER));
            stockTable.addCell(createCell(daysLeft + " days").setTextAlignment(TextAlignment.CENTER));
            stockTable.addCell(createCell(status).setBold().setFontColor(statusColor));
            stockTable.addCell(createCell(supplier));
        }

        doc.add(stockTable);

        // Alerts Section
        doc.add(new Paragraph("🔔 CRITICAL ALERTS")
                .setFontSize(18).setBold()
                .setFontColor(red)
                .setMarginTop(25)
                .setMarginBottom(15));

        // Expired
        PreparedStatement ps3 = conn.prepareStatement(
                "SELECT Vaccine_Name, Batch_Number, Quantity FROM vaccine_stock WHERE Expiry_Date < CURDATE()"
        );
        ResultSet rs3 = ps3.executeQuery();

        Paragraph expiredList = new Paragraph("☠️ Expired Vaccines:\n").setBold().setFontSize(12);
        boolean hasExpired = false;
        while (rs3.next()) {
            hasExpired = true;
            expiredList.add("• " + rs3.getString(1) + " (Batch: " + rs3.getString(2) +
                    ") - Qty: " + rs3.getInt(3) + "\n");
        }
        if (!hasExpired) expiredList.add("✅ None\n");
        doc.add(expiredList);

        // Low Stock
        PreparedStatement ps4 = conn.prepareStatement(
                "SELECT Vaccine_Name, Batch_Number, Quantity FROM vaccine_stock WHERE Quantity < 50 AND Quantity > 0"
        );
        ResultSet rs4 = ps4.executeQuery();

        Paragraph lowStockList = new Paragraph("\n⚠️ Low Stock (< 50 units):\n").setBold().setFontSize(12);
        boolean hasLowStock = false;
        while (rs4.next()) {
            hasLowStock = true;
            lowStockList.add("• " + rs4.getString(1) + " (Batch: " + rs4.getString(2) +
                    ") - Qty: " + rs4.getInt(3) + "\n");
        }
        if (!hasLowStock) lowStockList.add("✅ None\n");
        doc.add(lowStockList);

        // Footer
        doc.add(new Paragraph("\n\n" + "─".repeat(80))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20));

        doc.add(new Paragraph("Generated by Child Vaccination System • " + LocalDate.now())
                .setFontSize(10)
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(5));

        doc.close();
        return fileName;
    }

    private Cell createSummaryCell(String label, String value, DeviceRgb color) {
        Paragraph p = new Paragraph()
                .add(new Text(label + "\n").setFontSize(10).setFontColor(DeviceRgb.BLACK))
                .add(new Text(value).setFontSize(20).setBold().setFontColor(color));

        return new Cell()
                .add(p)
                .setPadding(15)
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(new SolidBorder(new DeviceRgb(200, 200, 200), 1));
    }

    private Cell createCell(String text) {
        return new Cell()
                .add(new Paragraph(text))
                .setPadding(8)
                .setBorder(new SolidBorder(new DeviceRgb(211, 211, 211), 1));
    }

    private String getStatus(int quantity, long daysLeft) {
        if (daysLeft < 0) return "☠️ Expired";
        if (daysLeft <= 30) return "⏰ Expiring Soon";
        if (quantity == 0) return "❌ Out of Stock";
        if (quantity < 50) return "⚠️ Low Stock";
        return "✅ Good Stock";
    }

    private DeviceRgb getStatusColor(int quantity, long daysLeft) {
        if (daysLeft < 0 || quantity == 0) return new DeviceRgb(220, 53, 69);
        if (daysLeft <= 30 || quantity < 50) return new DeviceRgb(255, 140, 0);
        return new DeviceRgb(0, 170, 0);
    }
}
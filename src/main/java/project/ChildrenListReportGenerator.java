package project;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import java.awt.Color;
import java.sql.*;
import java.time.LocalDate;

public class ChildrenListReportGenerator {
    private final Connection conn;
    public ChildrenListReportGenerator(Connection conn) { this.conn = conn; }

    public String generateReport(String province) throws Exception {
        String fileName = "Children_List_" + (province.equals("All") ? "All" : province) + "_" + LocalDate.now() + ".pdf";
        PdfWriter writer = new PdfWriter(fileName);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        doc.add(new Paragraph("Children Records Report - " + (province.equals("All") ? "All Provinces" : province))
                .setFontSize(20).setBold().setTextAlignment(TextAlignment.CENTER));

        String query = "SELECT c.Child_Id, c.C_Name, c.Gender, c.Date_of_Birth, c.Province, p.P_Name, p.P_Contact_No " +
                "FROM Children c JOIN Parents p ON c.Parent_Id = p.Parent_Id";
        if (!province.equals("All")) query += " WHERE c.Province = ?";
        query += " ORDER BY c.C_Name";

        PreparedStatement ps = conn.prepareStatement(query);
        if (!province.equals("All")) ps.setString(1, province);
        ResultSet rs = ps.executeQuery();

        Table table = new Table(7).useAllAvailableWidth().setMarginTop(20);
        String[] headers = {"ID", "Name", "Gender", "DOB", "Province", "Parent", "Contact"};
        for (String h : headers) {
            table.addHeaderCell(new Cell().add(new Paragraph(h))
                    .setBackgroundColor(new DeviceRgb(0, 102, 204))
                    .setFontColor(DeviceRgb.WHITE).setBold());
        }

        while (rs.next()) {
            for (int i = 1; i <= 7; i++) {
                table.addCell(new Cell().add(new Paragraph(rs.getString(i)))
                        .setBorder(new SolidBorder(new DeviceRgb(211, 211, 211), 1)));  // ← Fixed: LIGHT_GRAY → 211,211,211
            }
        }
        doc.add(table);
        doc.add(new Paragraph("Generated: " + LocalDate.now())
                .setFontSize(10).setItalic().setTextAlignment(TextAlignment.RIGHT));
        doc.close();
        return fileName;
    }
}

package project;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;


import java.sql.*;
import java.time.LocalDate;

public class UpcomingAppointmentsReportGenerator {
    private final Connection conn;

    public UpcomingAppointmentsReportGenerator(Connection conn) {
        this.conn = conn;
    }

    public String generateReport() throws Exception {
        String fileName = "Upcoming_Appointments_" + LocalDate.now() + ".pdf";
        PdfWriter writer = new PdfWriter(fileName);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        // Beautiful Title
        doc.add(new Paragraph("UPCOMING VACCINATION APPOINTMENTS")
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(new DeviceRgb(0, 102, 204))
                .setMarginBottom(20));

        doc.add(new Paragraph("Generated on: " + LocalDate.now())
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic()
                .setMarginBottom(20));

        // Correct Query - ALL Scheduled Appointments (Not just today!)
        String query = """
            SELECT c.C_Name, a.Vaccine_Name, a.Dose_Number,
                   a.Appointment_Date, a.Appointment_Time,
                   p.P_Contact_No, p.E_mail
            FROM appointments a
            JOIN children c ON a.Child_Id = c.Child_Id
            JOIN parents p ON c.Parent_Id = p.Parent_Id
            WHERE a.Status = 'Scheduled'
            ORDER BY a.Appointment_Date, a.Appointment_Time
            """;

        PreparedStatement ps = conn.prepareStatement(query);
        ResultSet rs = ps.executeQuery();

        // Table with 7 columns
        Table table = new Table(7).useAllAvailableWidth();
        table.setMarginTop(10);

        // Header
        String[] headers = {"Child Name", "Vaccine", "Dose", "Date", "Time", "Phone", "Email"};
        for (String h : headers) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(h).setBold().setFontColor(DeviceRgb.WHITE))
                    .setBackgroundColor(new DeviceRgb(0, 102, 204))
                    .setPadding(12)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        int count = 0;
        while (rs.next()) {
            count++;
            String dose = rs.getInt("Dose_Number") > 0 ? "Dose " + rs.getInt("Dose_Number") : "-";
            String time = rs.getString("Appointment_Time");
            if (time.length() > 5) time = time.substring(0, 5);

            table.addCell(new Cell().add(new Paragraph(rs.getString("C_Name"))).setPadding(10));
            table.addCell(new Cell().add(new Paragraph(rs.getString("Vaccine_Name"))).setPadding(10));
            table.addCell(new Cell().add(new Paragraph(dose)).setPadding(10).setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(rs.getDate("Appointment_Date").toString())).setPadding(10).setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(time)).setPadding(10).setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(rs.getString("P_Contact_No"))).setPadding(10));
            table.addCell(new Cell().add(new Paragraph(rs.getString("E_mail") != null ? rs.getString("E_mail") : "-")).setPadding(10));
        }

        if (count == 0) {
            table.addCell(new Cell(1, 7)
                    .add(new Paragraph("No upcoming appointments found.").setItalic().setFontSize(14))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(20));
        }

        doc.add(table);

        doc.add(new Paragraph("Total Appointments: " + count)
                .setFontSize(12)
                .setBold()
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(20));

        doc.close();
        return fileName;
    }
}
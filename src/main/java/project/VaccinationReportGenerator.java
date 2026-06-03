package project;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.io.image.ImageDataFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VaccinationReportGenerator {
    private final Connection conn;

    public VaccinationReportGenerator(Connection conn) {
        this.conn = conn;
    }

    public String generateReport(int childId) throws Exception {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT c.*, p.P_Name, p.P_Contact_No FROM Children c JOIN Parents p ON c.Parent_Id = p.Parent_Id WHERE c.Child_Id = ?"
        );
        ps.setInt(1, childId);
        ResultSet rs = ps.executeQuery();
        if (!rs.next()) return null;

        String childName = rs.getString("C_Name");
        String gender = rs.getString("Gender");
        String dob = rs.getString("Date_of_Birth");
        String province = rs.getString("Province");
        String address = rs.getString("Address");
        String parentName = rs.getString("P_Name");
        String parentContact = rs.getString("P_Contact_No");

        LocalDate birthDate = LocalDate.parse(dob);
        String fileName = "Report_" + childName.replace(" ", "_") + "_" + LocalDate.now() + ".pdf";

        PdfWriter writer = new PdfWriter(fileName);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        DeviceRgb blue = new DeviceRgb(0, 102, 204);
        DeviceRgb green = new DeviceRgb(0, 170, 0);
        DeviceRgb red = new DeviceRgb(200, 0, 0);
        DeviceRgb gray = new DeviceRgb(128, 128, 128);

        try {
            Image logo = new Image(ImageDataFactory.create("icon-childhood-vaccination-removebg-preview.png"));
            logo.scaleToFit(100, 100);
            logo.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
            doc.add(logo);
        } catch (Exception e) {
            doc.add(new Paragraph("VACCINATION SYSTEM")
                    .setFontSize(28).setBold().setTextAlignment(TextAlignment.CENTER));
        }

        doc.add(new Paragraph("CHILD VACCINATION REPORT")
                .setFontSize(24).setBold().setTextAlignment(TextAlignment.CENTER).setFontColor(blue));
        doc.add(new Paragraph("Government of Nepal • Ministry of Health")
                .setFontSize(12).setItalic().setTextAlignment(TextAlignment.CENTER));

        Table info = new Table(4).useAllAvailableWidth().setMarginTop(20);
        info.addCell(cell("Child Name", childName));
        info.addCell(cell("Gender", gender));
        info.addCell(cell("DOB", dob));
        info.addCell(cell("Parent", parentName));
        info.addCell(cell("Contact", parentContact));
        info.addCell(cell("Province", province));
        info.addCell(cell("Address", address, 2));
        doc.add(info);

        Table table = new Table(5).useAllAvailableWidth().setMarginTop(30);
        table.addHeaderCell(header("Vaccine"));
        table.addHeaderCell(header("Age"));
        table.addHeaderCell(header("Due Date"));
        table.addHeaderCell(header("Given On"));
        table.addHeaderCell(header("Status"));

        // ✅ Get vaccine schedules filtered by gender
        List<VaccineSchedule> schedules = getVaccineSchedules(birthDate, gender);

        for (VaccineSchedule schedule : schedules) {
            PreparedStatement cps = conn.prepareStatement(
                    "SELECT Completion_Date FROM Appointments WHERE Child_Id = ? AND Vaccine_Name = ? AND Dose_Number = ? AND Status = 'Completed'"
            );
            cps.setInt(1, childId);
            cps.setString(2, schedule.vaccineName);
            cps.setInt(3, schedule.doseNumber);
            ResultSet crs = cps.executeQuery();

            String given = "-";
            String status = "Pending";
            DeviceRgb statusColor = gray;

            if (crs.next()) {
                given = crs.getString("Completion_Date");
                status = "Completed";
                statusColor = green;
            } else {
                PreparedStatement mps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM Appointments WHERE Child_Id = ? AND Vaccine_Name = ? AND Dose_Number = ? AND Status = 'Missed'"
                );
                mps.setInt(1, childId);
                mps.setString(2, schedule.vaccineName);
                mps.setInt(3, schedule.doseNumber);
                ResultSet mrs = mps.executeQuery();

                if (mrs.next() && mrs.getInt(1) > 0) {
                    status = "Missed";
                    statusColor = red;
                } else if (schedule.dueDate == null) {
                    status = "Not Applicable";
                    statusColor = gray;
                } else {
                    status = "Pending";
                    statusColor = gray;
                }
            }

            String dueStr = (schedule.dueDate == null) ? "N/A" : schedule.dueDate.toString();
            String vaccineDisplay = schedule.totalDoses > 1 ?
                    schedule.vaccineName + " (Dose " + schedule.doseNumber + ")" :
                    schedule.vaccineName;

            table.addCell(body(vaccineDisplay));
            table.addCell(body(schedule.ageDisplay));
            table.addCell(body(dueStr));
            table.addCell(body(given));
            table.addCell(body(status).setFontColor(statusColor).setBold());
        }

        doc.add(table);

        // ✅ ADD GENDER-SPECIFIC NOTE
        if ("Male".equalsIgnoreCase(gender)) {
            doc.add(new Paragraph("\n📌 Note: HPV vaccine (for cervical cancer prevention) is not applicable for males.")
                    .setFontSize(10)
                    .setItalic()
                    .setFontColor(gray));
        } else if ("Female".equalsIgnoreCase(gender)) {
            doc.add(new Paragraph("\n📌 Note: Td vaccine is only for pregnant women and is not part of childhood immunization.")
                    .setFontSize(10)
                    .setItalic()
                    .setFontColor(gray));
        }

        doc.add(new Paragraph("Generated on: " + LocalDate.now())
                .setFontSize(10).setItalic().setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(10));
        doc.close();
        return fileName;
    }

    // ✅ CORRECTED: Filter vaccines based on gender
    private List<VaccineSchedule> getVaccineSchedules(LocalDate birthDate, String gender) {
        List<VaccineSchedule> schedules = new ArrayList<>();

        // Universal vaccines (for all children)
        schedules.add(new VaccineSchedule("BCG", "At birth", birthDate, 1, 1));
        schedules.add(new VaccineSchedule("DPT-HepB-Hib", "6th week", birthDate.plusWeeks(6), 1, 3));
        schedules.add(new VaccineSchedule("DPT-HepB-Hib", "10th week", birthDate.plusWeeks(10), 2, 3));
        schedules.add(new VaccineSchedule("DPT-HepB-Hib", "14th week", birthDate.plusWeeks(14), 3, 3));
        schedules.add(new VaccineSchedule("Oral Polio Vaccine (OPV)", "6th week", birthDate.plusWeeks(6), 1, 3));
        schedules.add(new VaccineSchedule("Oral Polio Vaccine (OPV)", "10th week", birthDate.plusWeeks(10), 2, 3));
        schedules.add(new VaccineSchedule("Oral Polio Vaccine (OPV)", "14th week", birthDate.plusWeeks(14), 3, 3));
        schedules.add(new VaccineSchedule("fIPV", "14th week", birthDate.plusWeeks(14), 1, 2));
        schedules.add(new VaccineSchedule("fIPV", "9th month", birthDate.plusMonths(9), 2, 2));
        schedules.add(new VaccineSchedule("Rota Virus", "6th week", birthDate.plusWeeks(6), 1, 2));
        schedules.add(new VaccineSchedule("Rota Virus", "10th week", birthDate.plusWeeks(10), 2, 2));
        schedules.add(new VaccineSchedule("PCV", "6th week", birthDate.plusWeeks(6), 1, 3));
        schedules.add(new VaccineSchedule("PCV", "10th week", birthDate.plusWeeks(10), 2, 3));
        schedules.add(new VaccineSchedule("PCV", "9th month", birthDate.plusMonths(9), 3, 3));
        schedules.add(new VaccineSchedule("Measles-Rubella (MR)", "9th month", birthDate.plusMonths(9), 1, 2));
        schedules.add(new VaccineSchedule("Measles-Rubella (MR)", "15th month", birthDate.plusMonths(15), 2, 2));
        schedules.add(new VaccineSchedule("TCV", "15th month", birthDate.plusMonths(15), 1, 1));
        schedules.add(new VaccineSchedule("Japanese Encephalitis (JE)", "12th month", birthDate.plusMonths(12), 1, 1));

        // ✅ GENDER-SPECIFIC VACCINES
        // HPV - Only for FEMALES (cervical cancer prevention)
        if ("Female".equalsIgnoreCase(gender)) {
            schedules.add(new VaccineSchedule("HPV", "Grade 6 / 10 years", birthDate.plusYears(10), 1, 1));
        }

        // ✅ Td - REMOVED for all children (only for pregnant women - not part of childhood immunization)
        // This vaccine is administered during pregnancy, not in childhood

        return schedules;
    }

    private Cell cell(String label, String value) { return cell(label, value, 1); }
    private Cell cell(String label, String value, int span) {
        return new Cell(1, span)
                .add(new Paragraph(label + ": " + value).setBold())
                .setPadding(8)
                .setBorder(new SolidBorder(new DeviceRgb(200, 200, 200), 1));
    }

    private Cell header(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold().setFontColor(DeviceRgb.WHITE))
                .setBackgroundColor(new DeviceRgb(0, 102, 204))
                .setPadding(10);
    }

    private Cell body(String text) {
        return new Cell().add(new Paragraph(text)).setPadding(8);
    }

    private static class VaccineSchedule {
        String vaccineName;
        String ageDisplay;
        LocalDate dueDate;
        int doseNumber;
        int totalDoses;

        VaccineSchedule(String vaccineName, String ageDisplay, LocalDate dueDate, int doseNumber, int totalDoses) {
            this.vaccineName = vaccineName;
            this.ageDisplay = ageDisplay;
            this.dueDate = dueDate;
            this.doseNumber = doseNumber;
            this.totalDoses = totalDoses;
        }
    }
}
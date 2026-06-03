package project;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.io.image.ImageDataFactory;

import java.time.LocalDate;

public class ImmunizationSchedulePDFGenerator {

    public String generate() throws Exception {
        String fileName = "Nepal_Immunization_Schedule_2025_" + LocalDate.now() + ".pdf";

        PdfWriter writer = new PdfWriter(fileName);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);


        doc.add(new Paragraph("NATIONAL IMMUNIZATION SCHEDULE - NEPAL 2025")
                .setFontSize(26)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(new DeviceRgb(0, 102, 51))
                .setMarginBottom(30));

        // Table
        Table table = new Table(8).useAllAvailableWidth();
        DeviceRgb green = new DeviceRgb(0, 102, 51);
        DeviceRgb light = new DeviceRgb(240, 255, 240);

        String[] headers = {"Sn.", "Vaccine", "Age of Vaccination", "Dose", "Frequency", "Route", "Site", "Diseases Prevented"};
        for (String h : headers) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(h).setBold().setFontColor(DeviceRgb.WHITE))
                    .setBackgroundColor(green)
                    .setPadding(12)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        String[][] data = {
                {"1", "BCG", "At birth", "0.05 ml", "Single dose", "ID", "Upper right arm", "Tuberculosis"},
                {"2", "DPT-HepB-Hib", "6th, 10th, 14th week", "0.5 ml", "3 doses", "IM", "Left thigh", "Diphtheria, Pertussis, Tetanus, Hepatitis B, Hib"},
                {"3", "Oral Polio Vaccine (OPV)", "6th, 10th, 14th week", "2 drops", "3 doses", "Oral", "By mouth", "Poliomyelitis"},
                {"4", "fIPV", "14th week & 9th month", "0.1 ml", "2 doses", "ID", "Upper right arm", "Poliomyelitis"},
                {"5", "Rota Virus", "6th & 10th week", "All of tube", "2 doses", "Oral", "Inner buccal site", "Rotavirus diarrhoea"},
                {"6", "PCV", "6th, 10th week & 9th month", "0.5 ml", "3 doses", "IM", "Middle right thigh", "Pneumococcal diseases"},
                {"7", "Measles-Rubella (MR)", "9th & 15th month", "0.5 ml", "2 doses", "SC", "Upper left arm", "Measles & Rubella"},
                {"8", "TCV", "15th month", "0.5 ml", "1 dose", "IM", "Middle left thigh", "Typhoid"},
                {"9", "Japanese Encephalitis (JE)", "12th month", "0.5 ml", "Single dose", "SC", "Upper right thigh", "Japanese Encephalitis"},
                {"10", "HPV", "Grade 6 girls & 10-yr non-school girls", "0.5 ml", "Single dose", "IM", "Upper left arm", "Cervical Cancer"},
                {"11", "Td", "Pregnant women", "0.5 ml", "Every pregnancy", "IM", "Upper left arm", "Tetanus & Diphtheria"}
        };

        for (int i = 0; i < data.length; i++) {
            for (String cell : data[i]) {
                table.addCell(new Cell()
                        .add(new Paragraph(cell))
                        .setPadding(10)
                        .setTextAlignment(i == 0 ? TextAlignment.CENTER : TextAlignment.LEFT)
                        .setBackgroundColor(i % 2 == 0 ? light : DeviceRgb.WHITE)
                        .setBorder(new SolidBorder(new DeviceRgb(200, 200, 200), 1)));
            }
        }

        doc.add(table);

        // Footer
        doc.add(new Paragraph("Generated on: " + LocalDate.now() + " | Child Vaccination System")
                .setFontSize(10)
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30));

        doc.close();
        return fileName;
    }
}
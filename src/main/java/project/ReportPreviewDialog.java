package project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportPreviewDialog extends JDialog {

    public ReportPreviewDialog(JFrame parent, Connection conn, int childId) {
        super(parent, "Vaccination Report Preview", true);
        setSize(1100, 750);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        String childName = "", dob = "", province = "", parentName = "", gender = "";
        LocalDate birthDate = null;

        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT c.C_Name, c.Date_of_Birth, c.Province, c.Gender, p.P_Name FROM Children c " +
                            "JOIN Parents p ON c.Parent_Id = p.Parent_Id WHERE c.Child_Id = ?"
            );
            ps.setInt(1, childId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                childName = rs.getString("C_Name");
                dob = rs.getString("Date_of_Birth");
                province = rs.getString("Province");
                gender = rs.getString("Gender"); // ✅ GET GENDER
                parentName = rs.getString("P_Name");
                birthDate = LocalDate.parse(dob);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading child data: " + e.getMessage());
            dispose();
            return;
        }

        // HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Vaccination Report - " + childName + " (" + gender + ")"); // ✅ Show gender
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JButton downloadBtn = new JButton("Download PDF");
        downloadBtn.setBackground(new Color(0, 170, 0));
        downloadBtn.setForeground(Color.WHITE);
        downloadBtn.setFont(new Font("Arial", Font.BOLD, 16));
        downloadBtn.setFocusPainted(false);
        downloadBtn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        downloadBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(downloadBtn, BorderLayout.EAST);

        // INFO PANEL
        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 10, 5));
        infoPanel.setBackground(new Color(240, 248, 255));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        infoPanel.add(new JLabel("Parent: " + parentName, JLabel.LEFT));
        infoPanel.add(new JLabel("Date of Birth: " + dob, JLabel.LEFT));
        infoPanel.add(new JLabel("Province: " + province, JLabel.LEFT));
        infoPanel.add(new JLabel("Generated: " + LocalDate.now(), JLabel.LEFT));

        // TABLE
        String[] columns = {"Vaccine Name", "Recommended Age", "Due Date", "Given On", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);

        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (column == 4) {
                    String status = (String) value;
                    setFont(new Font("Arial", Font.BOLD, 13));
                    switch (status) {
                        case "Completed" -> setForeground(new Color(0, 153, 0));
                        case "Missed"    -> setForeground(new Color(204, 0, 0));
                        default          -> setForeground(new Color(100, 100, 100));
                    }
                } else {
                    setForeground(Color.BLACK);
                }
                return c;
            }
        };
        for (int i = 0; i < 5; i++) table.getColumnModel().getColumn(i).setCellRenderer(statusRenderer);

        // ✅ PASS GENDER TO getVaccineSchedules
        List<VaccineSchedule> schedules = getVaccineSchedules(birthDate, gender);

        for (VaccineSchedule schedule : schedules) {
            try {
                PreparedStatement cps = conn.prepareStatement(
                        "SELECT Completion_Date FROM Appointments WHERE Child_Id = ? AND Vaccine_Name = ? AND Dose_Number = ? AND Status = 'Completed'"
                );
                cps.setInt(1, childId);
                cps.setString(2, schedule.vaccineName);
                cps.setInt(3, schedule.doseNumber);
                ResultSet crs = cps.executeQuery();

                String givenOn = "-";
                String status = "Pending";

                if (crs.next()) {
                    givenOn = crs.getString("Completion_Date");
                    status = "Completed";
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
                    } else if (schedule.dueDate == null) {
                        status = "Not Applicable";
                    } else {
                        status = "Pending";
                    }
                }

                String dueStr = (schedule.dueDate == null) ? "N/A" : schedule.dueDate.toString();
                String vaccineDisplay = schedule.totalDoses > 1 ?
                        schedule.vaccineName + " (Dose " + schedule.doseNumber + ")" :
                        schedule.vaccineName;

                model.addRow(new Object[]{vaccineDisplay, schedule.ageDisplay, dueStr, givenOn, status});
            } catch (Exception ignored) {}
        }

        // STATS PANEL
        int completed = 0, missed = 0, pending = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            String s = (String) model.getValueAt(i, 4);
            switch (s) {
                case "Completed" -> completed++;
                case "Missed"    -> missed++;
                default          -> pending++;
            }
        }

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setBackground(new Color(248, 249, 250));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        statsPanel.add(createStatBox("Completed", completed, new Color(0, 153, 0)));
        statsPanel.add(createStatBox("Missed", missed, new Color(204, 0, 0)));
        statsPanel.add(createStatBox("Pending / N/A", pending, new Color(100, 100, 100)));

        // ✅ ADD GENDER-SPECIFIC NOTE
        JPanel notePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        notePanel.setBackground(new Color(255, 255, 230));
        notePanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        String noteText = "";
        if ("Male".equalsIgnoreCase(gender)) {
            noteText = "📌 Note: HPV vaccine (for cervical cancer prevention) is not applicable for males.";
        } else if ("Female".equalsIgnoreCase(gender)) {
            noteText = "📌 Note: Td vaccine is only for pregnant women and is not part of childhood immunization.";
        }

        JLabel noteLabel = new JLabel("<html><i>" + noteText + "</i></html>");
        noteLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        noteLabel.setForeground(new Color(80, 80, 80));
        notePanel.add(noteLabel);

        // BOTTOM BUTTONS
        JPanel buttonPanel = new JPanel();
        JButton closeBtn = new JButton("Close");
        closeBtn.setBackground(new Color(108, 117, 125));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFont(new Font("Arial", Font.BOLD, 14));
        closeBtn.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        closeBtn.addActionListener(e -> dispose());
        buttonPanel.add(closeBtn);

        // DOWNLOAD ACTION
        downloadBtn.addActionListener(e -> {
            try {
                VaccinationReportGenerator gen = new VaccinationReportGenerator(conn);
                String file = gen.generateReport(childId);
                if (file != null) {
                    int opt = JOptionPane.showConfirmDialog(this,
                            "PDF Generated!\n" + file + "\nOpen now?", "Success",
                            JOptionPane.YES_NO_OPTION);
                    if (opt == JOptionPane.YES_OPTION) Desktop.getDesktop().open(new File(file));
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "PDF Error: " + ex.getMessage());
            }
        });

        // LAYOUT
        JPanel main = new JPanel(new BorderLayout());
        main.add(infoPanel, BorderLayout.NORTH);
        main.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(statsPanel, BorderLayout.NORTH);
        bottomPanel.add(notePanel, BorderLayout.CENTER);
        main.add(bottomPanel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);
        add(main, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createStatBox(String label, int count, Color color) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createLineBorder(color, 2));
        p.setBorder(BorderFactory.createCompoundBorder(
                p.getBorder(), BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel cnt = new JLabel(String.valueOf(count));
        cnt.setFont(new Font("Arial", Font.BOLD, 32));
        cnt.setForeground(color);
        cnt.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel txt = new JLabel(label);
        txt.setFont(new Font("Arial", Font.PLAIN, 14));
        txt.setForeground(Color.DARK_GRAY);
        txt.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(cnt);
        p.add(Box.createVerticalStrut(5));
        p.add(txt);
        return p;
    }

    // ✅ UPDATED: Filter vaccines based on gender
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

        // ✅ HPV - Only for FEMALES (cervical cancer prevention)
        if ("Female".equalsIgnoreCase(gender)) {
            schedules.add(new VaccineSchedule("HPV", "Grade 6 / 10 years", birthDate.plusYears(10), 1, 1));
        }

        // ✅ Td - REMOVED for all children (only for pregnant women)

        return schedules;
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
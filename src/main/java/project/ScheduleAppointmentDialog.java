
package project;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ScheduleAppointmentDialog extends JDialog {
    private Connection conn;
    private JComboBox<String> provinceCombo;
    private JComboBox<String> childCombo;
    private JComboBox<String> vaccineCombo;
    private JComboBox<Integer> doseCombo;
    private JLabel doseStatusLabel;
    private JLabel recommendedDateLabel;
    private JTextField dateField;
    private JComboBox<String> hourCombo, minuteCombo;
    private JSpinner dateAdjustSpinner;

    private Map<String, VaccineInfo> vaccineSchedule = new HashMap<>();
    private int currentChildId = -1;
    private LocalDate childBirthDate = null;
    private String childGender = null; // ✅ NEW: Track child gender

    public ScheduleAppointmentDialog(JFrame parent, Connection conn) {
        super(parent, "Smart Appointment Scheduling", true);
        this.conn = getValidConnection(conn);

        if (this.conn == null) {
            JOptionPane.showMessageDialog(parent, "Database connection failed!", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        initializeVaccineSchedule();
        setupUI();
        loadVaccines();

        setVisible(true);
    }

    private void initializeVaccineSchedule() {


        vaccineSchedule.put("BCG", new VaccineInfo(1, new int[]{0}, "DAYS"));

        // Week-based (use days)
        vaccineSchedule.put("DPT-HepB-Hib", new VaccineInfo(3, new int[]{42, 70, 98}, "DAYS"));
        vaccineSchedule.put("Oral Polio Vaccine (OPV)", new VaccineInfo(3, new int[]{42, 70, 98}, "DAYS"));
        vaccineSchedule.put("Rota Virus", new VaccineInfo(2, new int[]{42, 70}, "DAYS"));

        // Mixed (14 weeks in days, then months)
        vaccineSchedule.put("fIPV", new VaccineInfo(2, new int[]{98, -9}, "MIXED")); // -9 means 9 months
        vaccineSchedule.put("PCV", new VaccineInfo(3, new int[]{42, 70, -9}, "MIXED"));

        // Month-based (use negative to indicate months)
        vaccineSchedule.put("Measles-Rubella (MR)", new VaccineInfo(2, new int[]{-9, -15}, "MONTHS"));
        vaccineSchedule.put("TCV", new VaccineInfo(1, new int[]{-15}, "MONTHS"));
        vaccineSchedule.put("Japanese Encephalitis (JE)", new VaccineInfo(1, new int[]{-12}, "MONTHS"));
        vaccineSchedule.put("HPV", new VaccineInfo(1, new int[]{-120}, "MONTHS")); // 10 years = 120 months
        vaccineSchedule.put("Td", new VaccineInfo(1, new int[]{0}, "DAYS"));
    }

    private void setupUI() {
        setSize(700, 850);
        setLocationRelativeTo(getParent());
        getContentPane().setBackground(Color.WHITE);

        JPanel main = new JPanel(new GridBagLayout());
        main.setBorder(new EmptyBorder(30, 40, 30, 40));
        main.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel title = new JLabel("Smart Vaccination Scheduler", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(new Color(0, 102, 204));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        main.add(title, gbc);

        // Province Selection
        addLabel(main, "1️⃣ Select Province:", gbc, 1);
        String[] provinces = {"Koshi", "Madhesh", "Bagmati", "Gandaki", "Lumbini", "Karnali", "Sudurpashchim"};
        provinceCombo = new JComboBox<>(provinces);
        provinceCombo.setFont(new Font("Arial", Font.PLAIN, 15));
        provinceCombo.addActionListener(e -> loadChildren());
        gbc.gridx = 1;
        main.add(provinceCombo, gbc);

        // Child Selection
        addLabel(main, "2️⃣ Select Child:", gbc, 2);
        childCombo = new JComboBox<>();
        childCombo.setFont(new Font("Arial", Font.PLAIN, 15));
        childCombo.addActionListener(e -> onChildSelected());
        gbc.gridx = 1;
        main.add(childCombo, gbc);

        // Vaccine Selection
        addLabel(main, "3️⃣ Select Vaccine:", gbc, 3);
        vaccineCombo = new JComboBox<>();
        vaccineCombo.setFont(new Font("Arial", Font.PLAIN, 15));
        vaccineCombo.addActionListener(e -> onVaccineSelected());
        gbc.gridx = 1;
        main.add(vaccineCombo, gbc);

        // Dose Selection
        addLabel(main, "4️⃣ Select Dose:", gbc, 4);
        doseCombo = new JComboBox<>();
        doseCombo.setFont(new Font("Arial", Font.PLAIN, 15));
        doseCombo.addActionListener(e -> updateDoseStatus());
        gbc.gridx = 1;
        main.add(doseCombo, gbc);

        // Dose Status
        doseStatusLabel = new JLabel("");
        doseStatusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        main.add(doseStatusLabel, gbc);

        // Recommended Date
        recommendedDateLabel = new JLabel("");
        recommendedDateLabel.setFont(new Font("Arial", Font.BOLD, 15));
        recommendedDateLabel.setForeground(new Color(0, 120, 0));
        gbc.gridy = 6;
        main.add(recommendedDateLabel, gbc);

        // Date Adjustment
        addLabel(main, "Adjust Date (±days):", gbc, 7);
        dateAdjustSpinner = new JSpinner(new SpinnerNumberModel(0, -30, 30, 1));
        dateAdjustSpinner.setFont(new Font("Arial", Font.PLAIN, 15));
        dateAdjustSpinner.addChangeListener(e -> updateDateField());
        gbc.gridx = 1;
        main.add(dateAdjustSpinner, gbc);

        // Date Field
        addLabel(main, "Appointment Date (YYYY-MM-DD):", gbc, 8);
        dateField = new JTextField(20);
        dateField.setFont(new Font("Arial", Font.PLAIN, 16));
        dateField.setText(LocalDate.now().toString());
        gbc.gridx = 1;
        main.add(dateField, gbc);

        // Time Pickers
        addLabel(main, "Appointment Time:", gbc, 9);
        hourCombo = new JComboBox<>();
        minuteCombo = new JComboBox<>();

        for (int h = 8; h <= 18; h++)
            hourCombo.addItem(String.format("%02d", h));
        for (int m = 0; m < 60; m += 15)
            minuteCombo.addItem(String.format("%02d", m));

        hourCombo.setSelectedItem("10");
        minuteCombo.setSelectedItem("00");

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        timePanel.setBackground(Color.WHITE);
        timePanel.add(hourCombo);
        timePanel.add(new JLabel(":"));
        timePanel.add(minuteCombo);
        gbc.gridx = 1;
        main.add(timePanel, gbc);

        // Info Label
        JLabel infoLabel = new JLabel("<html><center>ℹ️ System calculates recommended date<br>You can manually adjust ±30 days</center></html>");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        infoLabel.setForeground(Color.GRAY);
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        main.add(infoLabel, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));
        btnPanel.setBackground(Color.WHITE);

        JButton saveBtn = createButton("Schedule", new Color(0, 170, 0));
        JButton cancelBtn = createButton("Cancel", new Color(220, 20, 60));

        saveBtn.addActionListener(e -> saveAppointment());
        cancelBtn.addActionListener(e -> dispose());

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        gbc.gridy = 11;
        main.add(btnPanel, gbc);

        add(main);
        loadChildren();
    }

    private void addLabel(JPanel panel, String text, GridBagConstraints gbc, int row) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(label, gbc);
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setPreferredSize(new Dimension(200, 50));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ✅ MODIFIED: Load child gender
    private void onChildSelected() {
        String selected = (String) childCombo.getSelectedItem();
        if (selected == null || selected.contains("No children")) return;

        try {
            String idPart = selected.split("\\|")[0].trim().replace("ID: ", "");
            currentChildId = Integer.parseInt(idPart);

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT Date_of_Birth, Gender FROM Children WHERE Child_Id = ?"
            );
            ps.setInt(1, currentChildId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                childBirthDate = LocalDate.parse(rs.getString("Date_of_Birth"));
                childGender = rs.getString("Gender"); // ✅ Store gender
            }

            loadVaccines(); // ✅ Reload vaccines based on gender
            onVaccineSelected();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onVaccineSelected() {
        String vaccine = (String) vaccineCombo.getSelectedItem();
        if (vaccine == null || childBirthDate == null) return;

        VaccineInfo info = vaccineSchedule.get(vaccine);
        if (info == null) return;

        doseCombo.removeAllItems();
        for (int i = 1; i <= info.totalDoses; i++) {
            doseCombo.addItem(i);
        }

        updateDoseStatus();
    }

    // ✅ MODIFIED: Show missed vaccine alert with health post contact
    private void updateDoseStatus() {
        if (currentChildId == -1 || doseCombo.getSelectedItem() == null) return;

        String vaccine = (String) vaccineCombo.getSelectedItem();
        int selectedDose = (int) doseCombo.getSelectedItem();

        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT Status, Completion_Date FROM Appointments " +
                            "WHERE Child_Id = ? AND Vaccine_Name = ? AND Dose_Number = ? AND Status = 'Completed'"
            );
            ps.setInt(1, currentChildId);
            ps.setString(2, vaccine);
            ps.setInt(3, selectedDose);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String date = rs.getString("Completion_Date");
                doseStatusLabel.setText("✅ Dose " + selectedDose + " already completed on " + date);
                doseStatusLabel.setForeground(new Color(0, 150, 0));
            } else {
                // ✅ CHECK FOR MISSED STATUS
                PreparedStatement ps2 = conn.prepareStatement(
                        "SELECT Appointment_Date FROM Appointments " +
                                "WHERE Child_Id = ? AND Vaccine_Name = ? AND Dose_Number = ? AND Status = 'Missed'"
                );
                ps2.setInt(1, currentChildId);
                ps2.setString(2, vaccine);
                ps2.setInt(3, selectedDose);
                ResultSet rs2 = ps2.executeQuery();

                if (rs2.next()) {
                    String date = rs2.getString("Appointment_Date");
                    doseStatusLabel.setText("<html>⚠️ <b>Dose " + selectedDose + " was MISSED</b> on " + date +
                            "<br>📞 Please contact nearest health post: <b>1115</b> (toll-free)</html>");
                    doseStatusLabel.setForeground(new Color(220, 20, 60));
                } else {
                    // Check if already scheduled
                    PreparedStatement ps3 = conn.prepareStatement(
                            "SELECT Appointment_Date FROM Appointments " +
                                    "WHERE Child_Id = ? AND Vaccine_Name = ? AND Dose_Number = ? AND Status = 'Scheduled'"
                    );
                    ps3.setInt(1, currentChildId);
                    ps3.setString(2, vaccine);
                    ps3.setInt(3, selectedDose);
                    ResultSet rs3 = ps3.executeQuery();

                    if (rs3.next()) {
                        String date = rs3.getString("Appointment_Date");
                        doseStatusLabel.setText("📅 Dose " + selectedDose + " already scheduled for " + date);
                        doseStatusLabel.setForeground(new Color(255, 140, 0));
                    } else {
                        doseStatusLabel.setText("⏳ Dose " + selectedDose + " not yet scheduled");
                        doseStatusLabel.setForeground(new Color(100, 100, 100));
                    }
                }
            }

            calculateRecommendedDate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calculateRecommendedDate() {
        String vaccine = (String) vaccineCombo.getSelectedItem();
        Integer selectedDose = (Integer) doseCombo.getSelectedItem();

        if (vaccine == null || selectedDose == null || childBirthDate == null) return;

        VaccineInfo info = vaccineSchedule.get(vaccine);
        if (info == null || selectedDose > info.totalDoses) return;

        int ageValue = info.ageSchedule[selectedDose - 1];
        LocalDate recommendedDate;
        String ageDisplay;

        if (ageValue < 0) {
            // Negative means months
            int months = Math.abs(ageValue);
            recommendedDate = childBirthDate.plusMonths(months);
            ageDisplay = months + " months";
        } else {
            // Positive means days
            recommendedDate = childBirthDate.plusDays(ageValue);
            ageDisplay = getAgeDisplay(ageValue);
        }

        recommendedDateLabel.setText("📌 Recommended Date: " + recommendedDate +
                " (" + ageDisplay + ")");

        updateDateField();
    }

    private void updateDateField() {
        String vaccine = (String) vaccineCombo.getSelectedItem();
        Integer selectedDose = (Integer) doseCombo.getSelectedItem();

        if (vaccine == null || selectedDose == null || childBirthDate == null) return;

        VaccineInfo info = vaccineSchedule.get(vaccine);
        if (info == null) return;

        int ageValue = info.ageSchedule[selectedDose - 1];
        int adjustment = (int) dateAdjustSpinner.getValue();
        LocalDate finalDate;

        // ✅ UNIFIED LOGIC: Negative values = months, Positive = days
        if (ageValue < 0) {
            // Negative means months (e.g., -9 = 9 months)
            int months = Math.abs(ageValue);
            finalDate = childBirthDate.plusMonths(months).plusDays(adjustment);
        } else {
            // Positive means days (e.g., 42 = 42 days)
            finalDate = childBirthDate.plusDays(ageValue + adjustment);
        }

        dateField.setText(finalDate.toString());
    }

    private String getAgeDisplay(int days) {
        if (days == 0) return "at birth";
        if (days < 7) return days + " days";
        if (days < 30) return (days / 7) + " weeks";
        if (days < 365) return (days / 30) + " months";
        return (days / 365) + " years";
    }

    private void saveAppointment() {
        if (currentChildId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a child!");
            return;
        }

        String vaccine = (String) vaccineCombo.getSelectedItem();
        Integer dose = (Integer) doseCombo.getSelectedItem();
        String date = dateField.getText().trim();
        String time = hourCombo.getSelectedItem() + ":" +
                minuteCombo.getSelectedItem() + ":00";

        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format! Please use YYYY-MM-DD\nExample: 2024-09-20",
                    "Invalid Date", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            PreparedStatement checkComplete = conn.prepareStatement(
                    "SELECT COUNT(*) FROM Appointments WHERE Child_Id = ? AND Vaccine_Name = ? " +
                            "AND Dose_Number = ? AND Status = 'Completed'"
            );
            checkComplete.setInt(1, currentChildId);
            checkComplete.setString(2, vaccine);
            checkComplete.setInt(3, dose);
            ResultSet rs = checkComplete.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this,
                        "❌ Dose " + dose + " is already completed!",
                        "Already Done", JOptionPane.WARNING_MESSAGE);
                return;
            }

            PreparedStatement checkScheduled = conn.prepareStatement(
                    "SELECT COUNT(*) FROM Appointments WHERE Child_Id = ? AND Vaccine_Name = ? " +
                            "AND Dose_Number = ? AND Status = 'Scheduled'"
            );
            checkScheduled.setInt(1, currentChildId);
            checkScheduled.setString(2, vaccine);
            checkScheduled.setInt(3, dose);
            ResultSet rs2 = checkScheduled.executeQuery();

            if (rs2.next() && rs2.getInt(1) > 0) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Dose " + dose + " is already scheduled.\nDo you want to reschedule?",
                        "Already Scheduled", JOptionPane.YES_NO_OPTION);

                if (confirm != JOptionPane.YES_OPTION) return;

                PreparedStatement delete = conn.prepareStatement(
                        "DELETE FROM Appointments WHERE Child_Id = ? AND Vaccine_Name = ? " +
                                "AND Dose_Number = ? AND Status = 'Scheduled'"
                );
                delete.setInt(1, currentChildId);
                delete.setString(2, vaccine);
                delete.setInt(3, dose);
                delete.executeUpdate();
            }

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Appointments (Child_Id, Vaccine_Name, Dose_Number, Appointment_Date, " +
                            "Appointment_Time, Status) VALUES (?, ?, ?, ?, ?, 'Scheduled')"
            );
            ps.setInt(1, currentChildId);
            ps.setString(2, vaccine);
            ps.setInt(3, dose);
            ps.setString(4, date);
            ps.setString(5, time);
            ps.executeUpdate();

            String childName = ((String) childCombo.getSelectedItem()).split("\\|")[1].trim();

            JOptionPane.showMessageDialog(this,
                    "✅ Appointment scheduled successfully!\n\n" +
                            "Child: " + childName + "\n" +
                            "Vaccine: " + vaccine + " (Dose " + dose + ")\n" +
                            "Date: " + date + " at " + time.substring(0, 5) + "\n\n" +
                            "📧 Reminder will be sent when you click 'Send Reminder' button",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadChildren() {
        try {
            String selectedProvince = (String) provinceCombo.getSelectedItem();
            String query = "SELECT Child_Id, C_Name FROM Children WHERE Province = ? ORDER BY C_Name";

            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, selectedProvince);

            ResultSet rs = ps.executeQuery();
            childCombo.removeAllItems();

            int count = 0;
            while (rs.next()) {
                int childId = rs.getInt("Child_Id");
                String childName = rs.getString("C_Name");
                childCombo.addItem("ID: " + childId + " | " + childName);
                count++;
            }

            if (count == 0) {
                childCombo.addItem("No children in " + selectedProvince);
                childCombo.setEnabled(false);
            } else {
                childCombo.setEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ MODIFIED: Filter vaccines based on gender
    private void loadVaccines() {
        vaccineCombo.removeAllItems();

        String[] orderedVaccines = {
                "BCG",
                "DPT-HepB-Hib",
                "Oral Polio Vaccine (OPV)",
                "fIPV",
                "Rota Virus",
                "PCV",
                "Measles-Rubella (MR)",
                "TCV",
                "Japanese Encephalitis (JE)",
                "HPV", // ✅ Will be filtered below
                "Td"
        };

        for (String vaccine : orderedVaccines) {
            // ✅ SKIP HPV FOR MALES
            if (vaccine.equals("HPV") && "Male".equalsIgnoreCase(childGender)) {
                continue; // Don't add HPV for males
            }
            vaccineCombo.addItem(vaccine);
        }
    }

    private Connection getValidConnection(Connection oldConn) {
        try {
            if (oldConn != null && !oldConn.isClosed()) return oldConn;
        } catch (Exception e) {
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/child_vaccination?useSSL=false&serverTimezone=UTC",
                    "root", ""
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class VaccineInfo {
        int totalDoses;
        int[] ageSchedule;
        String ageType; // "DAYS", "MONTHS", or "MIXED"

        VaccineInfo(int totalDoses, int[] ageSchedule, String ageType) {
            this.totalDoses = totalDoses;
            this.ageSchedule = ageSchedule;
            this.ageType = ageType;
        }
    }
}
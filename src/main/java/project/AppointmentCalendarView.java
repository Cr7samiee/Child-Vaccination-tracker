package project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.List;

public class AppointmentCalendarView extends JDialog {
    private Connection conn;
    private YearMonth currentMonth;
    private JPanel calendarPanel;
    private JLabel monthLabel;
    private Map<LocalDate, List<AppointmentInfo>> appointmentsByDate = new HashMap<>();

    public AppointmentCalendarView(JFrame parent, Connection conn) {
        super(parent, "📅 Appointment Calendar", true);
        this.conn = conn;
        this.currentMonth = YearMonth.now();
        setupUI();
        loadAppointments();
        setVisible(true);
    }

    private void setupUI() {
        setSize(1300, 850);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        // HEADER PANEL
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Month Navigation
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        navPanel.setOpaque(false);

        JButton prevBtn = createNavButton("Previous");
        JButton todayBtn = createNavButton("Today");
        JButton nextBtn = createNavButton("Next");

        monthLabel = new JLabel();
        monthLabel.setFont(new Font("Arial", Font.BOLD, 28));
        monthLabel.setForeground(Color.WHITE);
        updateMonthLabel();

        prevBtn.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            updateMonthLabel();
            loadAppointments();
        });

        todayBtn.addActionListener(e -> {
            currentMonth = YearMonth.now();
            updateMonthLabel();
            loadAppointments();
        });

        nextBtn.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            updateMonthLabel();
            loadAppointments();
        });

        navPanel.add(prevBtn);
        navPanel.add(monthLabel);
        navPanel.add(todayBtn);
        navPanel.add(nextBtn);

        // Legend
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        legendPanel.setOpaque(false);

        legendPanel.add(createLegendItem("Scheduled", new Color(33, 150, 243)));
        legendPanel.add(createLegendItem("Today", new Color(255, 152, 0)));
        legendPanel.add(createLegendItem("Completed", new Color(76, 175, 80)));

        headerPanel.add(navPanel, BorderLayout.CENTER);
        headerPanel.add(legendPanel, BorderLayout.SOUTH);

        // CALENDAR PANEL
        calendarPanel = new JPanel(new GridLayout(7, 7, 5, 5));
        calendarPanel.setBackground(Color.WHITE);
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Day headers
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : days) {
            JLabel header = new JLabel(day, JLabel.CENTER);
            header.setFont(new Font("Arial", Font.BOLD, 14));
            header.setForeground(new Color(100, 100, 100));
            header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
            calendarPanel.add(header);
        }

        // BUTTON PANEL
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonPanel.setBackground(Color.WHITE);

        JButton exportBtn = createButton("Export Month", new Color(0, 123, 255));
        JButton refreshBtn = createButton("Refresh", new Color(76, 175, 80));
        JButton closeBtn = createButton("Close", new Color(108, 117, 125));

        exportBtn.addActionListener(e -> exportCalendar());
        refreshBtn.addActionListener(e -> loadAppointments());
        closeBtn.addActionListener(e -> dispose());

        buttonPanel.add(exportBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(closeBtn);

        add(headerPanel, BorderLayout.NORTH);
        add(calendarPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0, 80, 160));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        return btn;
    }

    private JPanel createLegendItem(String text, Color color) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setOpaque(false);

        JLabel colorBox = new JLabel("  ");
        colorBox.setOpaque(true);
        colorBox.setBackground(color);
        colorBox.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setForeground(Color.WHITE);

        panel.add(colorBox);
        panel.add(label);
        return panel;
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        return btn;
    }

    private void updateMonthLabel() {
        monthLabel.setText(currentMonth.getMonth().toString() + " " + currentMonth.getYear());
    }

    private void loadAppointments() {
        appointmentsByDate.clear();

        try {
            LocalDate firstDay = currentMonth.atDay(1);
            LocalDate lastDay = currentMonth.atEndOfMonth();

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT a.Appointment_Id, a.Appointment_Date, a.Appointment_Time, a.Status, " +
                            "c.C_Name, a.Vaccine_Name, a.Dose_Number, p.P_Contact_No " +
                            "FROM Appointments a " +
                            "JOIN Children c ON a.Child_Id = c.Child_Id " +
                            "JOIN Parents p ON c.Parent_Id = p.Parent_Id " +
                            "WHERE a.Appointment_Date BETWEEN ? AND ? " +
                            "ORDER BY a.Appointment_Date, a.Appointment_Time"
            );
            ps.setString(1, firstDay.toString());
            ps.setString(2, lastDay.toString());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LocalDate date = LocalDate.parse(rs.getString("Appointment_Date"));
                AppointmentInfo info = new AppointmentInfo(
                        rs.getInt("Appointment_Id"),
                        rs.getString("C_Name"),
                        rs.getString("Vaccine_Name"),
                        rs.getInt("Dose_Number"),
                        rs.getString("Appointment_Time"),
                        rs.getString("Status"),
                        rs.getString("P_Contact_No")
                );

                appointmentsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(info);
            }

            renderCalendar();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading appointments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void renderCalendar() {
        // Remove old day cells (keep headers)
        while (calendarPanel.getComponentCount() > 7) {
            calendarPanel.remove(7);
        }

        LocalDate firstDay = currentMonth.atDay(1);
        int startDayOfWeek = firstDay.getDayOfWeek().getValue() % 7; // 0 = Sunday

        // Add empty cells before first day
        for (int i = 0; i < startDayOfWeek; i++) {
            calendarPanel.add(new JLabel(""));
        }

        // Add day cells
        for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
            LocalDate date = currentMonth.atDay(day);
            calendarPanel.add(createDayCell(date));
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private JPanel createDayCell(LocalDate date) {
        JPanel cell = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Determine background color
                Color bgColor = Color.WHITE;
                if (date.equals(LocalDate.now())) {
                    bgColor = new Color(255, 248, 225); // Light orange for today
                }

                g2d.setColor(bgColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // Border
                g2d.setColor(new Color(220, 220, 220));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
        };

        cell.setLayout(new BorderLayout(5, 5));
        cell.setOpaque(false);
        cell.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        cell.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Day number
        JLabel dayLabel = new JLabel(String.valueOf(date.getDayOfMonth()));
        dayLabel.setFont(new Font("Arial", Font.BOLD, 16));
        dayLabel.setForeground(date.equals(LocalDate.now()) ?
                new Color(255, 87, 34) : new Color(60, 60, 60));

        // Appointment count
        List<AppointmentInfo> appointments = appointmentsByDate.getOrDefault(date, new ArrayList<>());
        JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
        countPanel.setOpaque(false);

        if (!appointments.isEmpty()) {
            long scheduled = appointments.stream().filter(a -> a.status.equals("Scheduled")).count();
            long completed = appointments.stream().filter(a -> a.status.equals("Completed")).count();

            if (scheduled > 0) {
                JLabel schedLabel = new JLabel(scheduled + "");
                schedLabel.setFont(new Font("Arial", Font.BOLD, 11));
                schedLabel.setForeground(Color.WHITE);
                schedLabel.setOpaque(true);
                schedLabel.setBackground(new Color(33, 150, 243));
                schedLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
                countPanel.add(schedLabel);
            }

            if (completed > 0) {
                JLabel compLabel = new JLabel(completed + "");
                compLabel.setFont(new Font("Arial", Font.BOLD, 11));
                compLabel.setForeground(Color.WHITE);
                compLabel.setOpaque(true);
                compLabel.setBackground(new Color(76, 175, 80));
                compLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
                countPanel.add(compLabel);
            }
        }

        cell.add(dayLabel, BorderLayout.NORTH);
        cell.add(countPanel, BorderLayout.CENTER);

        // Click listener
        cell.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!appointments.isEmpty()) {
                    showAppointmentsForDate(date, appointments);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!appointments.isEmpty()) {
                    cell.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 204), 2));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                cell.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            }
        });

        return cell;
    }

    private void showAppointmentsForDate(LocalDate date, List<AppointmentInfo> appointments) {
        JDialog dialog = new JDialog(this, "Appointments - " + date, true);
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 204));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel(appointments.size() + " Appointments on " + date);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        header.add(titleLabel, BorderLayout.CENTER);

        // List
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (AppointmentInfo appt : appointments) {
            String doseText = appt.doseNumber > 0 ? " (Dose " + appt.doseNumber + ")" : "";
            String statusIcon = appt.status.equals("Completed") ? "✅" : "📅";
            listModel.addElement(String.format("%s %s - %s%s at %s [%s]",
                    statusIcon, appt.childName, appt.vaccine, doseText,
                    appt.time.substring(0, 5), appt.status));
        }

        JList<String> list = new JList<>(listModel);
        list.setFont(new Font("Arial", Font.PLAIN, 14));
        list.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(list);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 14));
        closeBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeBtn);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void exportCalendar() {
        try {
            String fileName = "Calendar_" + currentMonth.getMonth() + "_" +
                    currentMonth.getYear() + ".txt";
            java.io.FileWriter writer = new java.io.FileWriter(fileName);

            writer.write("APPOINTMENT CALENDAR\n");
            writer.write(currentMonth.getMonth() + " " + currentMonth.getYear() + "\n");
            writer.write("=".repeat(50) + "\n\n");

            for (Map.Entry<LocalDate, List<AppointmentInfo>> entry : appointmentsByDate.entrySet()) {
                writer.write(entry.getKey().toString() + "\n");
                for (AppointmentInfo appt : entry.getValue()) {
                    writer.write(String.format("  - %s: %s (Dose %d) at %s [%s]\n",
                            appt.childName, appt.vaccine, appt.doseNumber,
                            appt.time, appt.status));
                }
                writer.write("\n");
            }

            writer.close();
            JOptionPane.showMessageDialog(this, "Calendar exported to: " + fileName);
            Desktop.getDesktop().open(new java.io.File(fileName));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Export error: " + e.getMessage());
        }
    }

    static class AppointmentInfo {
        int id;
        String childName, vaccine, time, status, contact;
        int doseNumber;

        AppointmentInfo(int id, String child, String vaccine, int dose,
                        String time, String status, String contact) {
            this.id = id;
            this.childName = child;
            this.vaccine = vaccine;
            this.doseNumber = dose;
            this.time = time;
            this.status = status;
            this.contact = contact;
        }
    }
}

package project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

public class HealthWorkerDashboard extends JFrame {
    private int userId;
    private String userName, userNumber;
    private Connection conn;

    public HealthWorkerDashboard(int userId, String userName, String userNumber) {
        this.userId = userId;
        this.userName = userName;
        this.userNumber = userNumber;
        initDatabase();
        setupUI();
    }

    private void initDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/child_vaccination?useSSL=false&serverTimezone=UTC",
                    "root", ""
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupUI() {
        setTitle("Child Vaccination System - Health Worker Dashboard");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(240, 248, 255), 0, getHeight(), Color.WHITE);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 122, 204));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 120));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftHeader.setOpaque(false);

        JLabel logoLabel = new JLabel("💉");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 0));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Health Worker Portal");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(230, 240, 255));

        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);

        leftHeader.add(logoLabel);
        leftHeader.add(titlePanel);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        rightHeader.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome, " + userName);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);

        JButton logoutBtn = createHeaderButton("Logout", new Color(229, 57, 53));
        logoutBtn.addActionListener(e -> {
            dispose();
            new ChildVaccinationLogin();
        });

        rightHeader.add(welcomeLabel);
        rightHeader.add(logoutBtn);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);

        // **NEW: Statistics Panel**
        DashboardStatisticsPanel statsPanel = new DashboardStatisticsPanel(conn);

        // Grid Panel for Cards - NOW WITH NEW FEATURES
        JPanel gridPanel = new JPanel(new GridLayout(3, 3, 20, 20));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 40, 40));
        // Create Cards - INCLUDING NEW FEATURES
        gridPanel.add(createDashboardCard("👶", "Add Child &\nParent", "Register new family",
                new Color(33, 150, 243), e -> showAddChildDialog()));
        gridPanel.add(createDashboardCard("👥", "View Children\nRecords", "Search, Edit & PDF",
                new Color(34, 139, 34), e -> viewChildrenWithSearch()));

        gridPanel.add(createDashboardCard("📅", "Calendar\nView", "Visual appointment planning",
                new Color(255, 152, 0), e -> new AppointmentCalendarView(this, conn)));

        gridPanel.add(createDashboardCard("🗺️", "Coverage\nStatistics\n", "Coverage by region",
                new Color(0, 150, 136), e -> new ProvinceMapView(this, conn)));

        gridPanel.add(createDashboardCard("📅", "Smart\nScheduler", "Schedule Vaccinations",
                new Color(138, 43, 226), e -> new ScheduleAppointmentDialog(this, conn)));

        gridPanel.add(createDashboardCard("⏰", "Upcoming\nAppointments", "Mark Status + Send Alert",
                new Color(255, 165, 0), e -> viewUpcomingAppointmentsWithActions()));

        gridPanel.add(createDashboardCard("📊", "Vaccination\nReport", "Individual PDF",
                new Color(0, 128, 128), e -> generateReportWithProvinceFilter()));

        gridPanel.add(createDashboardCard("🗓️", "Immunization\nSchedule", "Official Nepal Schedule",
                new Color(0, 102, 51), e -> showImmunizationSchedule()));

        gridPanel.add(createDashboardCard("💉", "Vaccine\nStock", "Manage Inventory",
                new Color(220, 53, 69), e -> new VaccineStockManagement(this, conn)));

        // Layout with statistics panel
        JPanel contentPanel = new JPanel(new BorderLayout(0, 0));
        contentPanel.setOpaque(false);
        contentPanel.add(statsPanel, BorderLayout.NORTH);
        contentPanel.add(gridPanel, BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);



        add(mainPanel);
        setVisible(true);
    }
    private JPanel createSmallStatCard(String title, String value, String emoji, Color color) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 3),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        card.setLayout(new BorderLayout(10, 5));

        JLabel emojiLabel = new JLabel(emoji);
        emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 26));
        valueLabel.setForeground(color);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titleLabel.setForeground(Color.GRAY);

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(valueLabel, BorderLayout.NORTH);
        textPanel.add(titleLabel, BorderLayout.SOUTH);

        card.add(emojiLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }
    private JButton createHeaderButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(color.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(color);
            }
        });

        return btn;
    }

    private JPanel createDashboardCard(String icon, String title, String description,
                                       Color accentColor, ActionListener action) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow effect
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 20, 20);

                // Card background
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 20, 20);

                // Top accent bar
                g2d.setColor(accentColor);
                g2d.fillRoundRect(0, 0, getWidth() - 4, 8, 20, 20);
            }
        };

        card.setLayout(new BorderLayout(10, 10));
        card.setOpaque(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setPreferredSize(new Dimension(250, 200)); // ← FIXED SIZE

        // Icon at top - PROPER SIZE NOW
        JLabel iconLabel = new JLabel(icon, JLabel.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60)); // ← VISIBLE SIZE
        iconLabel.setPreferredSize(new Dimension(250, 80)); // ← ENSURE SPACE

        // Title and description
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(33, 33, 33));

        JLabel descLabel = new JLabel(description, JLabel.CENTER);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(new Color(117, 117, 117));

        textPanel.add(titleLabel);
        textPanel.add(descLabel);

        card.add(iconLabel, BorderLayout.CENTER);
        card.add(textPanel, BorderLayout.SOUTH);

        // Hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(accentColor, 2, true),
                        BorderFactory.createEmptyBorder(18, 18, 18, 18)
                ));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                action.actionPerformed(new ActionEvent(card, ActionEvent.ACTION_PERFORMED, null));
            }
        });

        return card;
    }
    private void showAddChildDialog() {
        new AddChildDialog(this, conn);


    }
    private void viewUpcomingAppointmentsWithActions() {
        JDialog d = new JDialog(this, "Upcoming Appointments", true);
        d.setSize(1500, 850);
        d.setLocationRelativeTo(this);

        String query = """
            SELECT a.Appointment_Id, c.C_Name, a.Vaccine_Name, a.Dose_Number,
                   DATE_FORMAT(a.Appointment_Date, '%Y-%m-%d') AS appointment_date,
                   DATE_FORMAT(a.Appointment_Time, '%h:%i %p') AS appointment_time,
                   p.P_Contact_No, p.E_mail, c.Child_Id
            FROM appointments a
            JOIN children c ON a.Child_Id = c.Child_Id
            JOIN parents p ON c.Parent_Id = p.Parent_Id
            WHERE a.Status = 'Scheduled'
            ORDER BY a.Appointment_Date, a.Appointment_Time
            """;

        DefaultTableModel model = new DefaultTableModel(new String[]{
                "Appointment ID", "Child Name", "Vaccine", "Dose", "Date", "Time", "Phone", "Email", "Child ID"
        }, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(55);
        table.setFont(new Font("Arial", Font.PLAIN, 17));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 19));
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);

        table.getColumnModel().getColumn(8).setMinWidth(0);
        table.getColumnModel().getColumn(8).setMaxWidth(0);
        table.getColumnModel().getColumn(8).setWidth(0);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(240, 255, 255) : Color.WHITE);
                } else {
                    c.setBackground(new Color(100, 200, 255));
                }
                setHorizontalAlignment(column <= 3 ? JLabel.CENTER : JLabel.LEFT);
                return c;
            }
        });

        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int doseNum = rs.getInt("Dose_Number");
                String doseDisplay = doseNum > 0 ? "Dose " + doseNum : "-";

                model.addRow(new Object[]{
                        rs.getInt("Appointment_Id"),
                        rs.getString("C_Name"),
                        rs.getString("Vaccine_Name"),
                        doseDisplay,
                        rs.getString("appointment_date"),
                        rs.getString("appointment_time"),
                        rs.getString("P_Contact_No"),
                        rs.getString("E_mail"),
                        rs.getInt("Child_Id")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(d, "Error loading data: " + e.getMessage());
            e.printStackTrace();
        }

        JScrollPane scroll = new JScrollPane(table);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));
        btnPanel.setBackground(Color.WHITE);

        JButton markBtn = new JButton("Mark as Completed");
        markBtn.setBackground(new Color(0, 150, 0));
        markBtn.setForeground(Color.WHITE);
        markBtn.setFont(new Font("Arial", Font.BOLD, 18));
        markBtn.setPreferredSize(new Dimension(250, 60));
        markBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(d, "Please select an appointment!"); return; }
            int appointmentId = (int) model.getValueAt(row, 0);
            int childId = (int) model.getValueAt(row, 8);
            String childName = (String) model.getValueAt(row, 1);
            String vaccine = (String) model.getValueAt(row, 2);
            String dose = (String) model.getValueAt(row, 3);
            String phone = (String) model.getValueAt(row, 6);
            String email = (String) model.getValueAt(row, 7);

            markAppointmentCompleted(appointmentId, childId, childName, vaccine, dose, phone, email);
            model.removeRow(row);
            JOptionPane.showMessageDialog(d, "Marked as Completed!\nSMS & Email sent to parent.");
        });

        JButton missedBtn = new JButton("Mark as Missed");
        missedBtn.setBackground(new Color(200, 0, 0));
        missedBtn.setForeground(Color.WHITE);
        missedBtn.setFont(new Font("Arial", Font.BOLD, 18));
        missedBtn.setPreferredSize(new Dimension(250, 60));
        missedBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(d, "Please select an appointment!"); return; }
            int appointmentId = (int) model.getValueAt(row, 0);
            String childName = (String) model.getValueAt(row, 1);
            String vaccine = (String) model.getValueAt(row, 2);
            String dose = (String) model.getValueAt(row, 3);
            String phone = (String) model.getValueAt(row, 6);
            String email = (String) model.getValueAt(row, 7);

            markAppointmentMissed(appointmentId, childName, vaccine, dose, phone, email);
            model.removeRow(row);
            JOptionPane.showMessageDialog(d, "Marked as Missed!\nSMS & Email sent to parent.");
        });

        JButton reminderBtn = new JButton("Send Reminder");
        reminderBtn.setBackground(new Color(0, 123, 255));
        reminderBtn.setForeground(Color.WHITE);
        reminderBtn.setFont(new Font("Arial", Font.BOLD, 18));
        reminderBtn.setPreferredSize(new Dimension(250, 60));
        reminderBtn.addActionListener(e -> {
            int[] rows = table.getSelectedRows();
            if (rows.length == 0) {
                JOptionPane.showMessageDialog(d, "Select at least one appointment!", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Confirmation dialog
            int confirm = JOptionPane.showConfirmDialog(d,
                    "Send reminder to " + rows.length + " parent(s)?\n\n" +
                            "This will send SMS and Email notifications.",
                    "Confirm Send Reminder",
                    JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) return;

            // Show progress dialog
            JDialog progressDialog = new JDialog(d, "Sending Reminders...", true);
            JProgressBar progressBar = new JProgressBar(0, rows.length);
            progressBar.setStringPainted(true);
            JLabel statusLabel = new JLabel("Preparing to send...", JLabel.CENTER);

            JPanel progressPanel = new JPanel(new BorderLayout(10, 10));
            progressPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            progressPanel.add(statusLabel, BorderLayout.NORTH);
            progressPanel.add(progressBar, BorderLayout.CENTER);

            progressDialog.add(progressPanel);
            progressDialog.setSize(400, 150);
            progressDialog.setLocationRelativeTo(d);

            // Send reminders in background thread
            SwingWorker<Void, Integer> worker = new SwingWorker<>() {
                int successCount = 0;
                int failCount = 0;
                StringBuilder failedList = new StringBuilder();

                @Override
                protected Void doInBackground() throws Exception {
                    for (int i = 0; i < rows.length; i++) {
                        int row = rows[i];

                        String childName = (String) model.getValueAt(row, 1);
                        String vaccine = (String) model.getValueAt(row, 2);
                        String dose = (String) model.getValueAt(row, 3);
                        String date = (String) model.getValueAt(row, 4);
                        String time = (String) model.getValueAt(row, 5);
                        String phone = (String) model.getValueAt(row, 6);
                        String email = (String) model.getValueAt(row, 7);

                        statusLabel.setText("Sending to " + childName + "...");

                        try {
                            // ✅ USE NEW METHOD - Sends both SMS and Email
                            Notifications.sendBothNotifications(
                                    phone,
                                    email,
                                    childName,
                                    vaccine,
                                    dose,
                                    date,
                                    time
                            );
                            successCount++;
                        } catch (Exception ex) {
                            failCount++;
                            failedList.append("• ").append(childName).append("\n");
                            ex.printStackTrace();
                        }

                        publish(i + 1);
                        Thread.sleep(1000); // Small delay between sends
                    }
                    return null;
                }

                @Override
                protected void process(java.util.List<Integer> chunks) {
                    int latest = chunks.get(chunks.size() - 1);
                    progressBar.setValue(latest);
                    progressBar.setString(latest + " / " + rows.length);
                }

                @Override
                protected void done() {
                    progressDialog.dispose();

                    // Show result summary
                    String resultMessage = String.format(
                            "<html><body style='width: 300px; font-family: Arial;'>" +
                                    "<h2 style='color: #28a745; text-align: center;'>✅ Reminders Sent!</h2>" +
                                    "<table style='width: 100%%; margin-top: 15px;'>" +
                                    "<tr><td><b>Total Selected:</b></td><td>%d</td></tr>" +
                                    "<tr><td><b>Successfully Sent:</b></td><td style='color: green;'>%d</td></tr>" +
                                    "<tr><td><b>Failed:</b></td><td style='color: red;'>%d</td></tr>" +
                                    "</table>" +
                                    (failCount > 0 ? "<hr><p style='color: red;'><b>Failed for:</b></p><p>" + failedList.toString() + "</p>" : "") +
                                    "</body></html>",
                            rows.length, successCount, failCount
                    );

                    JOptionPane.showMessageDialog(d,
                            new JLabel(resultMessage),
                            "Reminder Summary",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            };

            worker.execute();
            progressDialog.setVisible(true);
        });

        JButton pdfBtn = new JButton("Export to PDF");
        pdfBtn.setBackground(new Color(220, 53, 69));
        pdfBtn.setForeground(Color.WHITE);
        pdfBtn.setFont(new Font("Arial", Font.BOLD, 18));
        pdfBtn.setPreferredSize(new Dimension(250, 60));
        pdfBtn.addActionListener(e -> {
            try {
                UpcomingAppointmentsReportGenerator gen = new UpcomingAppointmentsReportGenerator(conn);
                String file = gen.generateReport();
                int opt = JOptionPane.showConfirmDialog(d, "PDF Generated!\n" + file + "\nOpen now?", "Success", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) Desktop.getDesktop().open(new File(file));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage());
            }
        });

        btnPanel.add(markBtn);
        btnPanel.add(missedBtn);
        btnPanel.add(reminderBtn);
        btnPanel.add(pdfBtn);

        d.add(scroll, BorderLayout.CENTER);
        d.add(btnPanel, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void markAppointmentCompleted(int appointmentId, int childId, String childName,
                                          String vaccine, String dose, String phone, String email) {
        try {
            // Update status in database
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE appointments SET Status = 'Completed', Completion_Date = CURDATE() WHERE Appointment_Id = ?");
            ps.setInt(1, appointmentId);
            ps.executeUpdate();

            String today = java.time.LocalDate.now().toString();

            // ✅ FIXED: Use the correct method from Notifications class
            Notifications.sendCompletedNotification(phone, email, childName, vaccine, dose, today);

            System.out.println("✅ Appointment marked as completed and notification sent");
            JOptionPane.showMessageDialog(this,
                    "✅ Appointment marked as Completed!\n\n" +
                            "📧 Notifications sent to parent:\n" +
                            "• SMS sent to: " + phone + "\n" +
                            "• Email sent to: " + (email != null && email.contains("@") ? email : "N/A"),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    private void markAppointmentMissed(int appointmentId, String childName, String vaccine,
                                       String dose, String phone, String email) {
        try {
            // Update status in database
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE appointments SET Status = 'Missed' WHERE Appointment_Id = ?");
            ps.setInt(1, appointmentId);
            ps.executeUpdate();

            // ✅ FIXED: Use the correct method from Notifications class
            Notifications.sendMissedNotification(phone, email, childName, vaccine, dose);

            System.out.println("⚠️ Appointment marked as missed and alert sent");
            JOptionPane.showMessageDialog(this,
                    "⚠️ Appointment marked as Missed!\n\n" +
                            "📧 Alert notifications sent to parent:\n" +
                            "• SMS sent to: " + phone + "\n" +
                            "• Email sent to: " + (email != null && email.contains("@") ? email : "N/A"),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void viewChildrenWithSearch() {
        JDialog d = new JDialog(this, "View & Search Children Records", true);
        d.setSize(1400, 800);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        // TOP PANEL WITH PROPER SPACING & BIG DELETE BUTTON
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.setBackground(new Color(240, 248, 255));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // LEFT SIDE: Search + Province
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        leftPanel.setOpaque(false);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JTextField searchField = new JTextField(22);
        searchField.setFont(new Font("Arial", Font.PLAIN, 15));

        JButton searchBtn = new JButton("Search");
        searchBtn.setBackground(new Color(0, 123, 255));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFont(new Font("Arial", Font.BOLD, 14));
        searchBtn.setFocusPainted(false);

        JLabel provinceLabel = new JLabel("Province:");
        provinceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        String[] provinces = {"All", "Koshi", "Madhesh", "Bagmati", "Gandaki", "Lumbini", "Karnali", "Sudurpashchim"};
        JComboBox<String> provinceCombo = new JComboBox<>(provinces);
        provinceCombo.setFont(new Font("Arial", Font.PLAIN, 15));

        leftPanel.add(searchLabel);
        leftPanel.add(searchField);
        leftPanel.add(searchBtn);
        leftPanel.add(provinceLabel);
        leftPanel.add(provinceCombo);

        // RIGHT SIDE: PDF + DELETE BUTTONS (BIG & CLEAR)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        rightPanel.setOpaque(false);

        JButton pdfBtn = new JButton("Download PDF");
        pdfBtn.setBackground(new Color(0, 140, 0));
        pdfBtn.setForeground(Color.WHITE);
        pdfBtn.setFont(new Font("Arial", Font.BOLD, 16));
        pdfBtn.setPreferredSize(new Dimension(180, 45));

        JButton deleteBtn = new JButton("DELETE CHILD");
        deleteBtn.setBackground(new Color(220, 20, 60));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFont(new Font("Arial", Font.BOLD, 18));
        deleteBtn.setPreferredSize(new Dimension(200, 50));
        deleteBtn.setFocusPainted(false);
        deleteBtn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        rightPanel.add(pdfBtn);
        rightPanel.add(deleteBtn);

        // Add both panels to top
        topPanel.add(leftPanel);
        topPanel.add(Box.createHorizontalGlue()); // pushes right panel to end
        topPanel.add(rightPanel);

        // TABLE SETUP
        DefaultTableModel model = new DefaultTableModel(new String[]{
                "Child ID", "Name", "DOB", "Gender", "Province", "Parent Name", "Contact", "Email"
        }, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(45);
        table.setFont(new Font("Arial", Font.PLAIN, 15));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(table);
        loadChildren(model, "", "All");

        // FILTER LOGIC
        ActionListener filterAction = e -> {
            String text = searchField.getText().trim();
            String prov = (String) provinceCombo.getSelectedItem();
            loadChildren(model, text, prov);
        };
        searchBtn.addActionListener(filterAction);
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) filterAction.actionPerformed(null);
            }
        });
        provinceCombo.addActionListener(e -> filterAction.actionPerformed(null));

        // PDF BUTTON
        pdfBtn.addActionListener(e -> {
            String prov = (String) provinceCombo.getSelectedItem();
            try {
                ChildrenListReportGenerator gen = new ChildrenListReportGenerator(conn);
                String file = gen.generateReport("All".equals(prov) ? "All" : prov);
                JOptionPane.showMessageDialog(d, "PDF Saved Successfully!\n" + file, "Success", JOptionPane.INFORMATION_MESSAGE);
                Desktop.getDesktop().open(new File(file));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "PDF Error: " + ex.getMessage());
            }
        });

        // DELETE BUTTON â€” NOW FULLY VISIBLE & SAFE
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(d, "Please select a child from the table!", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int childId = (Integer) model.getValueAt(row, 0);
            String childName = (String) model.getValueAt(row, 1);

            int confirm = JOptionPane.showConfirmDialog(d,
                    "<html><center><h2 style='color:red'>Delete Child Record?</h2>" +
                            "<b>Child:</b> " + childName + " (ID: " + childId + ")<br><br>" +
                            "This will permanently delete:<br>" +
                            "â€¢ Child record<br>" +
                            "â€¢ All vaccination history<br>" +
                            "â€¢ Parent account (if no other child)<br><br>" +
                            "<span style='color:red;font-weight:bold'>This action CANNOT be undone!</span></center></html>",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) return;

            try {
                conn.setAutoCommit(false);

                PreparedStatement delAppt = conn.prepareStatement("DELETE FROM appointments WHERE Child_Id = ?");
                delAppt.setInt(1, childId);
                delAppt.executeUpdate();

                PreparedStatement getParent = conn.prepareStatement("SELECT Parent_Id FROM children WHERE Child_Id = ?");
                getParent.setInt(1, childId);
                ResultSet rs = getParent.executeQuery();
                int parentId = rs.next() ? rs.getInt(1) : -1;

                PreparedStatement delChild = conn.prepareStatement("DELETE FROM children WHERE Child_Id = ?");
                delChild.setInt(1, childId);
                delChild.executeUpdate();

                if (parentId != -1) {
                    PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM children WHERE Parent_Id = ?");
                    check.setInt(1, parentId);
                    ResultSet rs2 = check.executeQuery();
                    if (rs2.next() && rs2.getInt(1) == 0) {
                        PreparedStatement delParent = conn.prepareStatement("DELETE FROM parents WHERE Parent_Id = ?");
                        delParent.setInt(1, parentId);
                        delParent.executeUpdate();
                    }
                }

                conn.commit();
                model.removeRow(row);
                JOptionPane.showMessageDialog(d, "Child record deleted successfully!", "Deleted", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                try { conn.rollback(); } catch (Exception ignored) {}
                JOptionPane.showMessageDialog(d, "Delete failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                try { conn.setAutoCommit(true); } catch (Exception ignored) {}
            }
        });

        // Double-click to edit
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        editChildRecord(
                                (Integer) model.getValueAt(row, 0),
                                (String) model.getValueAt(row, 1),
                                model.getValueAt(row, 2).toString(),
                                (String) model.getValueAt(row, 3),
                                (String) model.getValueAt(row, 4),
                                (String) model.getValueAt(row, 5),
                                (String) model.getValueAt(row, 6),
                                (String) model.getValueAt(row, 7),
                                model, row
                        );
                    }
                }
            }
        });

        d.add(topPanel, BorderLayout.NORTH);
        d.add(scroll, BorderLayout.CENTER);
        d.setVisible(true);
    }

    private void editChildRecord(int childId, String name, String dob, String gender,
                                 String province, String parentName, String contact, String email,
                                 DefaultTableModel model, int row) {
        JDialog editDialog = new JDialog(this, "Edit Child Record", true);
        editDialog.setSize(500, 600);
        editDialog.setLocationRelativeTo(this);
        editDialog.setLayout(new GridLayout(0, 2, 10, 10));
        editDialog.setResizable(false);

        JTextField nameField = new JTextField(name);
        JTextField dobField = new JTextField(dob);
        JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        genderBox.setSelectedItem(gender);
        JComboBox<String> provinceBox = new JComboBox<>(new String[]{
                "Koshi", "Madhesh", "Bagmati", "Gandaki", "Lumbini", "Karnali", "Sudurpashchim"
        });
        provinceBox.setSelectedItem(province);
        JTextField parentField = new JTextField(parentName);
        JTextField contactField = new JTextField(contact);
        JTextField emailField = new JTextField(email);

        editDialog.add(new JLabel(" Child Name:"));   editDialog.add(nameField);
        editDialog.add(new JLabel(" Date of Birth (YYYY-MM-DD):")); editDialog.add(dobField);
        editDialog.add(new JLabel(" Gender:"));       editDialog.add(genderBox);
        editDialog.add(new JLabel(" Province:"));     editDialog.add(provinceBox);
        editDialog.add(new JLabel(" Parent Name:"));  editDialog.add(parentField);
        editDialog.add(new JLabel(" Contact No:"));   editDialog.add(contactField);
        editDialog.add(new JLabel(" Email:"));        editDialog.add(emailField);

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setBackground(new Color(0, 150, 0));
        saveBtn.setForeground(Color.WHITE);
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(new Color(180, 180, 180));

        saveBtn.addActionListener(e -> {
            try {
                String newName = nameField.getText().trim();
                String newDob = dobField.getText().trim();
                String newGender = (String) genderBox.getSelectedItem();
                String newProvince = (String) provinceBox.getSelectedItem();
                String newParent = parentField.getText().trim();
                String newContact = contactField.getText().trim();
                String newEmail = emailField.getText().trim();

                if (newName.isEmpty() || newParent.isEmpty()) {
                    JOptionPane.showMessageDialog(editDialog, "Name and Parent Name cannot be empty!");
                    return;
                }

                // Update children table
                PreparedStatement ps1 = conn.prepareStatement(
                        "UPDATE children SET C_Name = ?, Date_of_Birth = ?, Gender = ?, Province = ? WHERE Child_Id = ?");
                ps1.setString(1, newName);
                ps1.setString(2, newDob);
                ps1.setString(3, newGender);
                ps1.setString(4, newProvince);
                ps1.setInt(5, childId);
                ps1.executeUpdate();

                // Update parents table (find Parent_Id first)
                PreparedStatement findParent = conn.prepareStatement(
                        "SELECT Parent_Id FROM children WHERE Child_Id = ?");
                findParent.setInt(1, childId);
                ResultSet rs = findParent.executeQuery();
                if (rs.next()) {
                    int parentId = rs.getInt(1);
                    PreparedStatement ps2 = conn.prepareStatement(
                            "UPDATE parents SET P_Name = ?, P_Contact_No = ?, E_mail = ? WHERE Parent_Id = ?");
                    ps2.setString(1, newParent);
                    ps2.setString(2, newContact);
                    ps2.setString(3, newEmail.isEmpty() ? null : newEmail);
                    ps2.setInt(4, parentId);
                    ps2.executeUpdate();
                }

                JOptionPane.showMessageDialog(editDialog, "Child record updated successfully!");
                editDialog.dispose();

                // Refresh table row
                model.setValueAt(newName, row, 1);
                model.setValueAt(newDob, row, 2);
                model.setValueAt(newGender, row, 3);
                model.setValueAt(newProvince, row, 4);
                model.setValueAt(newParent, row, 5);
                model.setValueAt(newContact, row, 6);
                model.setValueAt(newEmail, row, 7);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(editDialog, "Update failed: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        cancelBtn.addActionListener(e -> editDialog.dispose());
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        editDialog.add(new JLabel(""));
        editDialog.add(btnPanel);
        editDialog.setVisible(true);
    }

    private void loadChildren(DefaultTableModel model, String search, String province) {
        model.setRowCount(0);
        try {
            String sql = "SELECT c.Child_Id, c.C_Name, c.Date_of_Birth, c.Gender, c.Province, " +
                    "p.P_Name, p.P_Contact_No, p.E_mail " +
                    "FROM children c JOIN parents p ON c.Parent_Id = p.Parent_Id " +
                    "WHERE (LOWER(c.C_Name) LIKE ? OR c.Child_Id LIKE ?) " +
                    "AND (? = 'All' OR c.Province = ?) " +
                    "ORDER BY c.C_Name";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + search.toLowerCase() + "%");
            ps.setString(2, "%" + search + "%");
            ps.setString(3, province);
            ps.setString(4, province);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt(1), rs.getString(2), rs.getDate(3), rs.getString(4),
                        rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8)
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showImmunizationSchedule() {
        JDialog d = new JDialog(this, "National Immunization Schedule - Nepal 2025", true);
        d.setSize(1400, 800);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 51));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("NATIONAL IMMUNIZATION SCHEDULE - NEPAL 2025");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        JLabel subtitle = new JLabel("Government of Nepal - Ministry of Health");
        subtitle.setFont(new Font("Arial", Font.ITALIC, 14));
        subtitle.setForeground(new Color(230, 255, 230));
        subtitle.setHorizontalAlignment(JLabel.CENTER);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(subtitle);
        headerPanel.add(titlePanel, BorderLayout.CENTER);

        String[] columns = {"Sn.", "Name of Vaccine", "Age of vaccination", "Dose", "Frequency", "Route", "Site", "Diseases prevented"};
        Object[][] data = {
                {"1", "BCG", "At birth", "0.05 ml", "Single dose", "ID", "Upper right arm", "Tuberculosis"},
                {"2", "DPT, Hepatitis B, Hib", "6th, 10th and 14th week", "0.5 ml", "3 dose", "IM", "Left thigh", "Diphtheria, Pertussis, Tetanus, Hepatitis B, Meningitis and pneumonia"},
                {"3", "Oral Polio Vaccine (OPV)", "6th, 10th and 14th week", "2 drops", "3 dose", "Oral", "By mouth", "Poliomyelitis"},
                {"4", "fIPV", "14th week and 9 month", "0.1 ml", "2 dose", "ID", "Upper right arm", "Poliomyelitis"},
                {"5", "Rota virus", "6th and 10th week", "All of tube", "2 dose", "Oral", "Inner buccal site", "Diarrhea caused by Rota virus"},
                {"6", "PCV", "6th week, 10th week and 9th month", "0.5 ml", "3 dose", "IM", "Middle right thigh", "Meningitis and pneumonia caused by pneumococcal"},
                {"7", "Measles-Rubella (MR)", "9th month and 15th month", "0.5 ml", "2 dose", "SC", "Upper left arm", "Measles and Rubella"},
                {"8", "TCV", "15th month", "0.5 ml", "1 dose", "IM", "Middle left thigh", "Typhoid"},
                {"9", "JE", "12th month", "0.5 ml", "Single dose", "SC", "Upper right thigh", "Japanese Encephalitis"},
                {"10", "HPV", "A Grade 6 girl student and a 10-year-old adolescent girl who does not attend school", "0.5 ml", "Single dose", "IM", "Upper left arm", "Cervical Cancer"},
                {"11", "TD", "Pregnant women", "0.5 ml", "As soon as possible of pregnancy 1st dose, then interval of 1 month 2nd dose, then every gestation 1 dose", "IM", "Upper left arm", "Tetanus and Diphtheria"}
        };

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(50);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(0, 102, 51));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JButton closeBtn = new JButton("Close");
        closeBtn.setBackground(new Color(108, 117, 125));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFont(new Font("Arial", Font.BOLD, 14));
        closeBtn.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> d.dispose());
        footerPanel.add(closeBtn);

        d.add(headerPanel, BorderLayout.NORTH);
        d.add(scrollPane, BorderLayout.CENTER);
        d.add(footerPanel, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void generateReportWithProvinceFilter() {
        String[] provinces = {"Bagmati", "Gandaki", "Lumbini", "Koshi", "Madhesh", "Karnali", "Sudurpashchim"};
        String province = (String) JOptionPane.showInputDialog(this, "Select Province", "Generate Report",
                JOptionPane.QUESTION_MESSAGE, null, provinces, "Bagmati");
        if (province == null) return;

        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT Child_Id, C_Name FROM children WHERE Province = ? ORDER BY C_Name");
            ps.setString(1, province);
            ResultSet rs = ps.executeQuery();

            ArrayList<String> names = new ArrayList<>();
            ArrayList<Integer> ids = new ArrayList<>();
            while (rs.next()) {
                ids.add(rs.getInt(1));
                names.add(rs.getString(2));
            }
            if (names.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No children in " + province);
                return;
            }
            String child = (String) JOptionPane.showInputDialog(this, "Select Child", "Report",
                    JOptionPane.QUESTION_MESSAGE, null, names.toArray(), names.get(0));
            if (child != null) {
                int id = ids.get(names.indexOf(child));
                new ReportPreviewDialog(this, conn, id);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void changePassword() {
        JPasswordField old = new JPasswordField(20);
        JPasswordField nw = new JPasswordField(20);
        JPasswordField cnf = new JPasswordField(20);
        JPanel p = new JPanel(new GridLayout(0, 2, 10, 10));
        p.add(new JLabel("Current Password:")); p.add(old);
        p.add(new JLabel("New Password:")); p.add(nw);
        p.add(new JLabel("Confirm:")); p.add(cnf);

        int opt = JOptionPane.showConfirmDialog(this, p, "Change Password", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            String o = new String(old.getPassword());
            String n = new String(nw.getPassword());
            String c = new String(cnf.getPassword());
            if (!n.equals(c)) {
                JOptionPane.showMessageDialog(this, "Passwords don't match!");
                return;
            }
            try {
                PreparedStatement ps = conn.prepareStatement("SELECT Password FROM health_workers WHERE Worker_Id = ?");
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getString(1).equals(o)) {
                    PreparedStatement up = conn.prepareStatement("UPDATE health_workers SET Password = ? WHERE Worker_Id = ?");
                    up.setString(1, n);
                    up.setInt(2, userId);
                    up.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Password changed successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Wrong current password!");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    @Override
    public void dispose() {
        try { if (conn != null) conn.close(); }
        catch (Exception ignored) {}
        super.dispose();
    }
}
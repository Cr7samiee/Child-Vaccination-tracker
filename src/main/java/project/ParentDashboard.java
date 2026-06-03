
package project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;
import java.time.LocalDate;
public class ParentDashboard extends JFrame {
    private int userId;
    private String userName, userNumber;
    private Connection conn;

    public ParentDashboard(int id, String name, String number) {
        this.userId = id;
        this.userName = name;
        this.userNumber = number;
        initDatabase();
        setupUI();
    }

    private void initDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/child_vaccination?useSSL=false&serverTimezone=UTC", "root", "");
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupUI() {
        setTitle("Child Vaccination System - Parent Dashboard");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main Panel with gradient background
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
        headerPanel.setBackground(new Color(0, 122, 204)); // Blue background
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 120));

// Left: Logo and Title
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftHeader.setOpaque(false);

        JLabel logoLabel = new JLabel("💉");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 0));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE); // ← NOW WHITE!

        JLabel subtitleLabel = new JLabel("Parent Portal"); // Or "Health Worker Portal"
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(230, 240, 255)); // ← LIGHT BLUE/WHITE

        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);

        leftHeader.add(logoLabel);
        leftHeader.add(titlePanel);

// Right: Welcome and Logout
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        rightHeader.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome, " + userName);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE); // ← NOW WHITE!

        JButton logoutBtn = createHeaderButton("Logout", new Color(229, 57, 53));
        logoutBtn.addActionListener(e -> {
            dispose();
            new ChildVaccinationLogin();
        });

        rightHeader.add(welcomeLabel);
        rightHeader.add(logoutBtn);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);

        // Grid Panel for Cards
        JPanel gridPanel = new JPanel(new GridLayout(2, 3, 30, 30));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(40, 80, 80, 80));

        // Create Cards
        gridPanel.add(createDashboardCard("👶", "Add Child", "Register new child",
                new Color(33, 150, 243), e -> showAddChildrenForm()));

        gridPanel.add(createDashboardCard("✏️", "Update Child", "Edit child details",
                new Color(76, 175, 80), e -> showUpdateChildrenForm()));

        gridPanel.add(createDashboardCard("🗑️", "Delete Child", "Remove record",
                new Color(244, 67, 54), e -> showDeleteChildrenForm()));

        gridPanel.add(createDashboardCard("📄", "Vaccination Report", "View & download PDF",
                new Color(156, 39, 176), e -> showVaccinationReport()));

        gridPanel.add(createDashboardCard("📅", "Immunization Schedule", "Official Nepal timeline",
                new Color(255, 152, 0), e -> showImmunizationSchedule()));

        gridPanel.add(createDashboardCard("🔐", "Change Password", "Update your password",
                new Color(96, 125, 139), e -> showChangePasswordForm()));

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(gridPanel, BorderLayout.CENTER);

        add(mainPanel);
        setVisible(true);
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

        card.setLayout(new BorderLayout(10, 15));
        card.setOpaque(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setBorder(BorderFactory.createEmptyBorder(30, 25, 30, 25));

        // Icon at top
        JLabel iconLabel = new JLabel(icon, JLabel.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));

        // Title and description
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(33, 33, 33));

        JLabel descLabel = new JLabel(description, JLabel.CENTER);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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
                        BorderFactory.createEmptyBorder(28, 23, 28, 23)
                ));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createEmptyBorder(30, 25, 30, 25));
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                action.actionPerformed(new ActionEvent(card, ActionEvent.ACTION_PERFORMED, null));
            }
        });

        return card;
    }

    private void showAddChildrenForm() {
        JTextField nameField = new JTextField(20);
        String[] genders = {"Male", "Female", "Other"};
        JComboBox<String> genderCombo = new JComboBox<>(genders);
        JTextField dobField = new JTextField(20);
        dobField.setToolTipText("YYYY-MM-DD");
        String[] provinces = {"Koshi", "Madhesh", "Bagmati", "Gandaki", "Lumbini", "Karnali", "Sudurpashchim"};
        JComboBox<String> provinceCombo = new JComboBox<>(provinces);
        JTextField addressField = new JTextField(20);

        JPanel panel = new JPanel(new GridLayout(0, 2, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(createFormLabel("Child Name:")); panel.add(nameField);
        panel.add(createFormLabel("Gender:")); panel.add(genderCombo);
        panel.add(createFormLabel("Date of Birth (YYYY-MM-DD):")); panel.add(dobField);
        panel.add(createFormLabel("Province:")); panel.add(provinceCombo);
        panel.add(createFormLabel("Address:")); panel.add(addressField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Child",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            addChildToDatabase(nameField.getText(), (String) genderCombo.getSelectedItem(),
                    dobField.getText(), (String) provinceCombo.getSelectedItem(), addressField.getText());
        }
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return label;
    }

    private void addChildToDatabase(String name, String gender, String dob, String province, String address) {

        try {
            LocalDate birthDate = LocalDate.parse(dob);
            LocalDate today = LocalDate.now();

            if (birthDate.isAfter(today)) {
                JOptionPane.showMessageDialog(this,
                        "Date of Birth cannot be in the future!",
                        "Invalid Date", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (birthDate.isBefore(LocalDate.of(1900, 1, 1))) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid birth year (after 1900).",
                        "Invalid Date", JOptionPane.ERROR_MESSAGE);
                return;
            }

            long ageYears = java.time.temporal.ChronoUnit.YEARS.between(birthDate, today);
            if (ageYears > 18) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "This child would be " + ageYears + " years old.\n" +
                                "Are you sure this date is correct?",
                        "Confirm Age",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }

        } catch (java.time.format.DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format!\n\n" +
                            "Please use YYYY-MM-DD format\n" +
                            "Example: 2012-03-25",
                    "Invalid Date", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Children (Parent_Id, C_Name, Gender, Date_of_Birth, Province, Address) VALUES (?, ?, ?, ?, ?, ?)");
            ps.setInt(1, userId);
            ps.setString(2, name);
            ps.setString(3, gender);
            ps.setString(4, dob);
            ps.setString(5, province);
            ps.setString(6, address);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Child added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "❌ Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showUpdateChildrenForm() {
        Vector<Integer> childIds = new Vector<>();
        Vector<String> childNames = new Vector<>();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT Child_Id, C_Name FROM Children WHERE Parent_Id = ?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                childIds.add(rs.getInt("Child_Id"));
                childNames.add(rs.getString("C_Name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading children!");
            return;
        }

        if (childNames.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No children registered yet!");
            return;
        }

        JComboBox<String> combo = new JComboBox<>(childNames.toArray(new String[0]));
        int choice = JOptionPane.showConfirmDialog(this, combo, "Select Child to Update", JOptionPane.OK_CANCEL_OPTION);
        if (choice != JOptionPane.OK_OPTION) return;

        int childId = childIds.get(combo.getSelectedIndex());

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Children WHERE Child_Id = ?");
            ps.setInt(1, childId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                JTextField nameField = new JTextField(rs.getString("C_Name"), 20);
                String[] genders = {"Male", "Female", "Other"};
                JComboBox<String> genderCombo = new JComboBox<>(genders);
                genderCombo.setSelectedItem(rs.getString("Gender"));
                JTextField dobField = new JTextField(rs.getString("Date_of_Birth"), 20);
                String[] provinces = {"Koshi", "Madhesh", "Bagmati", "Gandaki", "Lumbini", "Karnali", "Sudurpashchim"};
                JComboBox<String> provinceCombo = new JComboBox<>(provinces);
                provinceCombo.setSelectedItem(rs.getString("Province"));
                JTextField addressField = new JTextField(rs.getString("Address"), 20);

                JPanel panel = new JPanel(new GridLayout(0, 2, 15, 15));
                panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                panel.add(createFormLabel("Child Name:")); panel.add(nameField);
                panel.add(createFormLabel("Gender:")); panel.add(genderCombo);
                panel.add(createFormLabel("Date of Birth:")); panel.add(dobField);
                panel.add(createFormLabel("Province:")); panel.add(provinceCombo);
                panel.add(createFormLabel("Address:")); panel.add(addressField);

                int result = JOptionPane.showConfirmDialog(this, panel, "Update Child Details", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    PreparedStatement updatePs = conn.prepareStatement(
                            "UPDATE Children SET C_Name=?, Gender=?, Date_of_Birth=?, Province=?, Address=? WHERE Child_Id=?");
                    updatePs.setString(1, nameField.getText());
                    updatePs.setString(2, (String) genderCombo.getSelectedItem());
                    updatePs.setString(3, dobField.getText());
                    updatePs.setString(4, (String) provinceCombo.getSelectedItem());
                    updatePs.setString(5, addressField.getText());
                    updatePs.setInt(6, childId);
                    updatePs.executeUpdate();
                    JOptionPane.showMessageDialog(this, "✅ Child updated successfully!");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void showDeleteChildrenForm() {
        Vector<Integer> childIds = new Vector<>();
        Vector<String> childNames = new Vector<>();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT Child_Id, C_Name FROM Children WHERE Parent_Id = ?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                childIds.add(rs.getInt("Child_Id"));
                childNames.add(rs.getString("C_Name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading children!");
            return;
        }

        if (childNames.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No children registered yet!");
            return;
        }

        JComboBox<String> combo = new JComboBox<>(childNames.toArray(new String[0]));
        int choice = JOptionPane.showConfirmDialog(this, combo, "Select Child to Delete", JOptionPane.OK_CANCEL_OPTION);
        if (choice != JOptionPane.OK_OPTION) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "⚠️ Are you sure you want to delete this child's record?\nThis action cannot be undone!",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        int childId = childIds.get(combo.getSelectedIndex());

        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM Children WHERE Child_Id = ?");
            ps.setInt(1, childId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Child deleted successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void showVaccinationReport() {
        Vector<Integer> childIds = new Vector<>();
        Vector<String> childNames = new Vector<>();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT Child_Id, C_Name FROM Children WHERE Parent_Id = ?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                childIds.add(rs.getInt("Child_Id"));
                childNames.add(rs.getString("C_Name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading children!");
            return;
        }

        if (childNames.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No children registered yet!");
            return;
        }

        JComboBox<String> combo = new JComboBox<>(childNames.toArray(new String[0]));
        int choice = JOptionPane.showConfirmDialog(this, combo, "Select Child for Report", JOptionPane.OK_CANCEL_OPTION);
        if (choice != JOptionPane.OK_OPTION) return;

        int childId = childIds.get(combo.getSelectedIndex());
        new ReportPreviewDialog(this, conn, childId);
    }

    private void showImmunizationSchedule() {
        JDialog d = new JDialog(this, "National Immunization Schedule - Nepal 2025", true);
        d.setSize(1400, 800);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 118, 210));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("NATIONAL IMMUNIZATION SCHEDULE - NEPAL 2025");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        JLabel subtitle = new JLabel("Government of Nepal - Ministry of Health");
        subtitle.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        subtitle.setForeground(new Color(230, 242, 255));
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
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(50);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(25, 118, 210));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JButton closeBtn = new JButton("Close");
        closeBtn.setBackground(new Color(96, 125, 139));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> d.dispose());

        footerPanel.add(closeBtn);

        d.add(headerPanel, BorderLayout.NORTH);
        d.add(scrollPane, BorderLayout.CENTER);
        d.add(footerPanel, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void showChangePasswordForm() {
        JPasswordField currentField = new JPasswordField(20);
        JPasswordField newField = new JPasswordField(20);
        JPasswordField confirmField = new JPasswordField(20);

        JPanel panel = new JPanel(new GridLayout(0, 2, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(createFormLabel("Current Password:")); panel.add(currentField);
        panel.add(createFormLabel("New Password:")); panel.add(newField);
        panel.add(createFormLabel("Confirm Password:")); panel.add(confirmField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Change Password", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String current = new String(currentField.getPassword());
            String newPass = new String(newField.getPassword());
            String confirm = new String(confirmField.getPassword());

            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!");
                return;
            }

            if (!newPass.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Passwords don't match!");
                return;
            }

            if (newPass.length() < 4) {
                JOptionPane.showMessageDialog(this, "Password must be at least 4 characters!");
                return;
            }

            try {
                PreparedStatement ps = conn.prepareStatement("SELECT Password FROM Parents WHERE Parent_Id = ?");
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getString(1).equals(current)) {
                    PreparedStatement up = conn.prepareStatement("UPDATE Parents SET Password = ? WHERE Parent_Id = ?");
                    up.setString(1, newPass);
                    up.setInt(2, userId);
                    up.executeUpdate();
                    JOptionPane.showMessageDialog(this, "✅ Password changed successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Current password is incorrect!");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    @Override
    public void dispose() {
        try {
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        super.dispose();
    }
}



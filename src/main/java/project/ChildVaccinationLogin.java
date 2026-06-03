
package project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ChildVaccinationLogin extends JFrame {
    private Connection conn;
    private String currentRole = "Parent";

    public ChildVaccinationLogin() {
        initDatabase();
        setupModernUI();
    }

    private void initDatabase() {
        try {
            System.out.println("Loading MySQL JDBC Driver...");
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✓ MySQL JDBC Driver loaded successfully!");

            String url = "jdbc:mysql://localhost:3306/child_vaccination?useSSL=false&serverTimezone=UTC";
            String user = "root";
            String password = "";

            System.out.println("Attempting connection to: " + url);
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("✓ Database connected successfully!");

        } catch (ClassNotFoundException e) {
            System.err.println("✗ ClassNotFoundException: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "MySQL Driver NOT FOUND!\n\nPlease add MySQL Connector/J to your classpath.\n\nError: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("✗ SQLException: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Database Connection Failed!\n\nPlease verify:\nMySQL Server is running (port 3306)\nDatabase 'child_vaccination' exists\nUsername is 'root' with NO password\n\nError: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("✗ Exception: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "⚠ Unexpected Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupModernUI() {
        setTitle("Child Vaccination Tracker - Login");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel with clean white background
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // TOP PANEL - Header with Exit
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(0, 122, 204));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Left side - Logo and Title
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        leftHeader.setOpaque(false);

        JLabel logoLabel = new JLabel("💉");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Child Vaccination Tracker");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Protecting Children, Building Future");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(230, 240, 255));

        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);

        leftHeader.add(logoLabel);
        leftHeader.add(titlePanel);

        // Right side - Exit button
        JButton exitBtn = new JButton("Exit");
        exitBtn.setBackground(new Color(220, 53, 69));
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setFocusPainted(false);
        exitBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        exitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exitBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20)); // ← REDUCED padding
        exitBtn.setPreferredSize(new Dimension(80, 35)); // ← FIXED small size
        exitBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to exit?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        topPanel.add(leftHeader, BorderLayout.WEST);
        topPanel.add(exitBtn, BorderLayout.EAST);

        // CENTER PANEL - Role Selection Cards
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(60, 80, 60, 80));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        // Title
        JLabel selectRoleLabel = new JLabel("Select Your Role");
        selectRoleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        selectRoleLabel.setForeground(new Color(33, 33, 33));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        centerPanel.add(selectRoleLabel, gbc);

        // Parent Card
        JPanel parentCard = createRoleCard(
                "👨‍👩‍👧",
                "Parent Portal",
                "Manage your children's vaccination records",
                new Color(33, 150, 243),
                e -> {
                    currentRole = "Parent";
                    showLoginForm();
                }
        );

        // Health Worker Card
        JPanel healthWorkerCard = createRoleCard(
                "👩‍⚕️",
                "Health Worker Portal",
                "Schedule and track vaccinations",
                new Color(76, 175, 80),
                e -> {
                    currentRole = "Health Worker";
                    showLoginForm();
                }
        );

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        centerPanel.add(parentCard, gbc);

        gbc.gridx = 1;
        centerPanel.add(healthWorkerCard, gbc);

        // FOOTER
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel footerLabel = new JLabel("© 2025 Child Vaccination System - Government of Nepal");
        footerLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        footerLabel.setForeground(new Color(150, 150, 150));
        footerPanel.add(footerLabel);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createRoleCard(String emoji, String title, String description, Color accentColor, ActionListener action) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow
                g2d.setColor(new Color(0, 0, 0, 15));
                g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 25, 25);

                // Card background
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth() - 10, getHeight() - 10, 25, 25);

                // Border
                g2d.setColor(new Color(230, 230, 230));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, getWidth() - 10, getHeight() - 10, 25, 25);
            }
        };

        card.setLayout(new BorderLayout(0, 20));
        card.setOpaque(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        card.setPreferredSize(new Dimension(300, 320)); // ← REDUCED from 350x380

        // Emoji icon - SMALLER
        JLabel iconLabel = new JLabel(emoji, JLabel.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 70)); // ← REDUCED from 80

        // Text panel
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22)); // ← REDUCED from 24
        titleLabel.setForeground(new Color(33, 33, 33));

        JLabel descLabel = new JLabel("<html><center>" + description + "</center></html>", JLabel.CENTER);
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
                        BorderFactory.createLineBorder(accentColor, 3, true),
                        BorderFactory.createEmptyBorder(37, 37, 37, 37)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                action.actionPerformed(new ActionEvent(card, ActionEvent.ACTION_PERFORMED, null));
            }
        });

        return card;
    }

    private void showLoginForm() {
        JDialog loginDialog = new JDialog(this, currentRole + " Login", true);
        loginDialog.setSize(520, 680);
        loginDialog.setLocationRelativeTo(this);
        loginDialog.getContentPane().setBackground(Color.WHITE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 45, 25, 45));

        // ========== HEADER ==========
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(currentRole + " Login", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 122, 204));

        JLabel subtitleLabel = new JLabel("Please enter your credentials", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(117, 117, 117));

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);

        // ========== FORM PANEL - Using GridBagLayout for better control ==========
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 15, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;
        gbc.weightx = 1;

        // Phone Number Label
        JLabel phoneLabel = new JLabel("Phone Number");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        phoneLabel.setForeground(new Color(60, 60, 60));
        gbc.gridy = 0;
        formPanel.add(phoneLabel, gbc);

        // Phone Number Field
        JTextField phoneField = new JTextField(20);
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        phoneField.setPreferredSize(new Dimension(400, 42)); // ✅ Fixed size
        phoneField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 2, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        gbc.gridy = 1;
        formPanel.add(phoneField, gbc);

        // Password Label
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        passLabel.setForeground(new Color(60, 60, 60));
        gbc.gridy = 2;
        gbc.insets = new Insets(18, 0, 8, 0); // ✅ Extra space above password
        formPanel.add(passLabel, gbc);

        // Password Field
        JPasswordField passField = new JPasswordField(20);
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        passField.setPreferredSize(new Dimension(400, 42)); // ✅ Fixed size
        passField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 2, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        gbc.gridy = 3;
        gbc.insets = new Insets(8, 0, 8, 0);
        formPanel.add(passField, gbc);

        // ✅ FIXED: Highly Visible Forgot Password Button
        JPanel forgotPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        forgotPanel.setOpaque(false);

        JButton forgotPasswordBtn = new JButton("<html><u>Forgot Password?</u></html>");
        forgotPasswordBtn.setFont(new Font("Segoe UI", Font.BOLD, 15)); // ✅ Bigger font
        forgotPasswordBtn.setForeground(new Color(220, 53, 69)); // ✅ RED for visibility
        forgotPasswordBtn.setBorderPainted(false);
        forgotPasswordBtn.setContentAreaFilled(false);
        forgotPasswordBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPasswordBtn.setFocusPainted(false);

        // Hover effect
        forgotPasswordBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                forgotPasswordBtn.setForeground(new Color(255, 0, 0)); // Brighter red
                forgotPasswordBtn.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Slightly bigger
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                forgotPasswordBtn.setForeground(new Color(220, 53, 69));
                forgotPasswordBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
            }
        });

        forgotPasswordBtn.addActionListener(e -> {
            loginDialog.dispose();
            new ForgotPasswordDialog(this, currentRole);
        });

        forgotPanel.add(forgotPasswordBtn);

        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 0, 0, 0);
        formPanel.add(forgotPanel, gbc);

        // ========== BUTTONS PANEL ==========
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(25, 0, 0, 0));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        JButton loginBtn = createStyledButton("LOGIN", new Color(0, 122, 204), Color.WHITE);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> performLogin(phoneField.getText(), new String(passField.getPassword()), loginDialog));

        JButton signupBtn = createStyledButton("Create New Account", new Color(76, 175, 80), Color.WHITE);
        signupBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        signupBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        signupBtn.addActionListener(e -> showRegistrationForm());

        JButton backBtn = createStyledButton("Back", new Color(158, 158, 158), Color.WHITE);
        backBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.addActionListener(e -> loginDialog.dispose());

        buttonPanel.add(loginBtn);
        buttonPanel.add(Box.createVerticalStrut(12));
        buttonPanel.add(signupBtn);
        buttonPanel.add(Box.createVerticalStrut(12));
        buttonPanel.add(backBtn);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        loginDialog.add(mainPanel);
        loginDialog.setVisible(true);
    }

    // ✅ Make sure this method exists (should already be in your code)
    private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(fgColor);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });

        return btn;
    }

    private void performLogin(String number, String pass, JDialog dialog) {
        if (number == null || number.trim().isEmpty() || pass == null || pass.trim().isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (conn == null || conn.isClosed()) {
                System.out.println("⚠ Connection lost. Reconnecting...");
                initDatabase();
                if (conn == null || conn.isClosed()) {
                    JOptionPane.showMessageDialog(dialog, "Database connection failed!\nPlease restart the application.",
                            "Connection Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Connection check failed: " + e.getMessage());
            return;
        }

        try {
            String table = currentRole.equals("Parent") ? "Parents" : "Health_Workers";
            String idCol = currentRole.equals("Parent") ? "Parent_Id" : "Worker_Id";
            String nameCol = currentRole.equals("Parent") ? "P_Name" : "H_Name";
            String contactCol = currentRole.equals("Parent") ? "P_Contact_No" : "H_Contact_No";

            String query = "SELECT " + idCol + ", " + nameCol + " FROM " + table +
                    " WHERE " + contactCol + " = ? AND Password = ?";

            System.out.println("🔍 Executing: " + query);
            System.out.println("📞 Phone: " + number);
            System.out.println("🔐 Password length: " + pass.length());

            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, number.trim());
            ps.setString(2, pass);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt(idCol);
                String userName = rs.getString(nameCol);

                System.out.println("✓ Login successful! User: " + userName);

                dialog.dispose();
                this.dispose();

                if (currentRole.equals("Parent")) {
                    SwingUtilities.invokeLater(() -> new ParentDashboard(userId, userName, number));
                } else {
                    SwingUtilities.invokeLater(() -> new HealthWorkerDashboard(userId, userName, number));
                }
            } else {
                System.out.println("✗ Login failed - Invalid credentials");
                JOptionPane.showMessageDialog(dialog,
                        "Invalid phone number or password!\n\nPlease check your credentials.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.err.println("✗ SQL Error: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(dialog,
                    "Login Error: " + e.getMessage() + "\n\nPlease contact support.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showRegistrationForm() {
        new RegistrationDialog(this, currentRole);
    }

    // ============= UPDATED REGISTRATION DIALOG CLASS =============
// Replace the RegistrationDialog class in ChildVaccinationLogin.java with this:

    class RegistrationDialog extends JDialog {
        public RegistrationDialog(JFrame parent, String role) {
            super(parent, role + " Registration", true);
            setSize(550, role.equals("Parent") ? 700 : 700); // ✅ SAME HEIGHT NOW
            setLocationRelativeTo(parent);
            getContentPane().setBackground(Color.WHITE);

            // Main container with BorderLayout
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(Color.WHITE);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

            // Header
            JLabel titleLabel = new JLabel("Create Account", JLabel.CENTER);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
            titleLabel.setForeground(new Color(0, 122, 204));
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));

            // ============= CENTERED FORM PANEL =============
            JPanel formPanel = new JPanel();
            formPanel.setOpaque(false);
            formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
            formPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

            // Create text fields
            JTextField nameF = new JTextField();
            JTextField phoneF = new JTextField();
            JTextField emailF = new JTextField(); // ✅ EMAIL FOR BOTH NOW
            JTextField extraF = new JTextField();
            JPasswordField passF = new JPasswordField();
            JPasswordField confirmF = new JPasswordField();

            // Add centered field panels
            formPanel.add(createCenteredFieldPanel("Name:", nameF));
            formPanel.add(Box.createVerticalStrut(12));

            formPanel.add(createCenteredFieldPanel("Phone:", phoneF));
            formPanel.add(Box.createVerticalStrut(12));

            // ✅ EMAIL FIELD FOR BOTH PARENT AND HEALTH WORKER
            formPanel.add(createCenteredFieldPanel("Email:", emailF));
            formPanel.add(Box.createVerticalStrut(12));

            if (role.equals("Parent")) {
                formPanel.add(createCenteredFieldPanel("Address:", extraF));
            } else {
                formPanel.add(createCenteredFieldPanel("Designation:", extraF));
            }
            formPanel.add(Box.createVerticalStrut(12));

            formPanel.add(createCenteredFieldPanel("Password:", passF));
            formPanel.add(Box.createVerticalStrut(12));

            formPanel.add(createCenteredFieldPanel("Confirm:", confirmF));

            // Buttons Panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
            buttonPanel.setOpaque(false);
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(25, 0, 0, 0));

            JButton regBtn = createStyledButton("REGISTER", new Color(76, 175, 80), Color.WHITE);
            regBtn.setPreferredSize(new Dimension(160, 45));

            JButton cancelBtn = createStyledButton("Cancel", new Color(158, 158, 158), Color.WHITE);
            cancelBtn.setPreferredSize(new Dimension(160, 45));

            regBtn.addActionListener(e -> {
                String pass = new String(passF.getPassword());
                String confirm = new String(confirmF.getPassword());

                if (nameF.getText().trim().isEmpty() || phoneF.getText().trim().isEmpty() || pass.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill all required fields!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!pass.equals(confirm)) {
                    JOptionPane.showMessageDialog(this, "Passwords don't match!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (pass.length() < 4) {
                    JOptionPane.showMessageDialog(this, "Password must be at least 4 characters!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // ✅ VALIDATE EMAIL FORMAT
                String email = emailF.getText().trim();
                if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid email address!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                registerUser(nameF.getText().trim(), phoneF.getText().trim(),
                        emailF.getText().trim(), extraF.getText().trim(), pass);
            });

            cancelBtn.addActionListener(e -> dispose());

            buttonPanel.add(regBtn);
            buttonPanel.add(cancelBtn);

            mainPanel.add(titleLabel, BorderLayout.NORTH);
            mainPanel.add(formPanel, BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            add(mainPanel);
            setVisible(true);
        }

        // ============= NEW CENTERED FIELD METHOD =============
        private JPanel createCenteredFieldPanel(String labelText, JComponent field) {
            // Outer wrapper to center everything
            JPanel wrapper = new JPanel(new GridBagLayout());
            wrapper.setOpaque(false);
            wrapper.setMaximumSize(new Dimension(500, 70));

            // Inner panel with label and field
            JPanel panel = new JPanel(new BorderLayout(15, 5));
            panel.setOpaque(false);
            panel.setPreferredSize(new Dimension(380, 60));

            // Label
            JLabel label = new JLabel(labelText);
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            label.setForeground(new Color(60, 60, 60));
            label.setPreferredSize(new Dimension(90, 25));

            // Field styling
            field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            field.setPreferredSize(new Dimension(260, 38));
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));

            panel.add(label, BorderLayout.WEST);
            panel.add(field, BorderLayout.CENTER);

            // Center the panel using GridBagConstraints
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            wrapper.add(panel, gbc);

            return wrapper;
        }

        private void registerUser(String name, String phone, String email, String extra, String pass) {
            try {
                if (conn == null || conn.isClosed()) {
                    initDatabase();
                }

                String table = currentRole.equals("Parent") ? "Parents" : "Health_Workers";

                String checkQuery = "SELECT COUNT(*) FROM " + table +
                        " WHERE " + (currentRole.equals("Parent") ? "P_Contact_No" : "H_Contact_No") + " = ?";
                PreparedStatement checkPs = conn.prepareStatement(checkQuery);
                checkPs.setString(1, phone);
                ResultSet checkRs = checkPs.executeQuery();

                if (checkRs.next() && checkRs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this,
                            "This phone number is already registered!\nPlease use a different number.",
                            "Registration Failed", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String query;
                PreparedStatement ps;

                if (currentRole.equals("Parent")) {
                    query = "INSERT INTO " + table + " (P_Name, P_Contact_No, E_mail, Address, Password) VALUES (?, ?, ?, ?, ?)";
                    ps = conn.prepareStatement(query);
                    ps.setString(1, name);
                    ps.setString(2, phone);
                    ps.setString(3, email.isEmpty() ? null : email);
                    ps.setString(4, extra.isEmpty() ? null : extra);
                    ps.setString(5, pass);
                } else {
                    // ✅ UPDATED: Now includes H_Email for Health Workers
                    query = "INSERT INTO " + table + " (H_Name, H_Contact_No, H_Email, H_Designation, Password) VALUES (?, ?, ?, ?, ?)";
                    ps = conn.prepareStatement(query);
                    ps.setString(1, name);
                    ps.setString(2, phone);
                    ps.setString(3, email.isEmpty() ? null : email); // ✅ EMAIL FOR HEALTH WORKER
                    ps.setString(4, extra.isEmpty() ? "Health Worker" : extra);
                    ps.setString(5, pass);
                }

                System.out.println("📝 Registering: " + name + " | Phone: " + phone + " | Email: " + email);
                ps.executeUpdate();
                ps.close();

                JOptionPane.showMessageDialog(this,
                        "✅ Registration Successful!\n\nYou can now login with:\nPhone: " + phone +
                                "\n\n💡 Email is required for password recovery.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                this.dispose();

            } catch (SQLException ex) {
                System.err.println("❌ Registration Error: " + ex.getMessage());
                ex.printStackTrace();

                String errorMsg = "Registration Error!\n\n";
                if (ex.getMessage().contains("H_Email")) {
                    errorMsg += "⚠️ Email column not found in database!\n\n" +
                            "Please run this SQL command first:\n" +
                            "ALTER TABLE health_workers ADD COLUMN H_Email VARCHAR(100) AFTER H_Contact_No;\n\n" +
                            "Then try registering again.";
                } else {
                    errorMsg += ex.getMessage() + "\n\nPlease check:\n- Phone number format\n- Database connection";
                }

                JOptionPane.showMessageDialog(this, errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChildVaccinationLogin::new);
    }
}
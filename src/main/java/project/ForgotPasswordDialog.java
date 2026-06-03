package project;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.Random;

public class ForgotPasswordDialog extends JDialog {
    private Connection conn;
    private String userType;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    private JTextField phoneField;
    private JTextField otpField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel timerLabel;
    private JLabel emailDisplayLabel;
    private Timer countdownTimer;
    private int timeRemaining = 300;

    private String verifiedPhone = null;
    private String verifiedEmail = null;
    private String generatedOTP = null;

    public ForgotPasswordDialog(JFrame parent, String userType) {
        super(parent, "Forgot Password - " + userType, true);
        this.userType = userType;

        initDatabase();
        setupUI();
        setVisible(true);
    }

    private void initDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/child_vaccination?useSSL=false&serverTimezone=UTC",
                    "root", ""
            );
            System.out.println("✅ Database connected for Forgot Password");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupUI() {
        setSize(550, 500);
        setLocationRelativeTo(getParent());
        setResizable(false);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createStep1Panel(), "STEP1");
        mainPanel.add(createStep2Panel(), "STEP2");
        mainPanel.add(createStep3Panel(), "STEP3");

        add(mainPanel);
    }

    // ============= STEP 1: PHONE NUMBER =============
    private JPanel createStep1Panel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 35, 25, 35));

        JLabel titleLabel = new JLabel("Password Recovery", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(0, 122, 204));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JPanel formPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel phoneLabel = new JLabel("Enter Phone Number:");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        phoneField = new JTextField();
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        phoneField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel infoLabel = new JLabel("<html><i>We'll send OTP to your registered email</i></html>");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoLabel.setForeground(Color.GRAY);

        formPanel.add(phoneLabel);
        formPanel.add(phoneField);
        formPanel.add(infoLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);

        JButton sendBtn = createButton("Send OTP", new Color(0, 122, 204));
        JButton cancelBtn = createButton("Cancel", new Color(108, 117, 125));

        sendBtn.addActionListener(e -> sendOTP());
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(sendBtn);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ============= STEP 2: OTP =============
    private JPanel createStep2Panel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 35, 25, 35));

        JLabel titleLabel = new JLabel("Enter OTP", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(0, 122, 204));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JPanel formPanel = new JPanel(new GridLayout(5, 1, 8, 8));
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        emailDisplayLabel = new JLabel("", JLabel.CENTER);
        emailDisplayLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emailDisplayLabel.setForeground(new Color(0, 122, 204));

        JLabel otpLabel = new JLabel("Enter 6-digit OTP:");
        otpLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        otpField = new JTextField();
        otpField.setFont(new Font("Segoe UI", Font.BOLD, 20));
        otpField.setHorizontalAlignment(JTextField.CENTER);
        otpField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 122, 204), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        timerLabel = new JLabel("⏱️ Time: 05:00", JLabel.CENTER);
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        timerLabel.setForeground(new Color(0, 150, 0));

        JButton resendBtn = new JButton("<html><u>Resend OTP</u></html>");
        resendBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        resendBtn.setForeground(new Color(0, 122, 204));
        resendBtn.setBorderPainted(false);
        resendBtn.setContentAreaFilled(false);
        resendBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resendBtn.addActionListener(e -> resendOTP());

        formPanel.add(emailDisplayLabel);
        formPanel.add(otpLabel);
        formPanel.add(otpField);
        formPanel.add(timerLabel);
        formPanel.add(resendBtn);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);

        JButton verifyBtn = createButton("Verify", new Color(0, 122, 204));
        JButton backBtn = createButton("Back", new Color(108, 117, 125));

        verifyBtn.addActionListener(e -> verifyOTP());
        backBtn.addActionListener(e -> {
            stopTimer();
            cardLayout.show(mainPanel, "STEP1");
        });

        buttonPanel.add(backBtn);
        buttonPanel.add(verifyBtn);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ============= STEP 3: NEW PASSWORD =============
    private JPanel createStep3Panel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 35, 25, 35));

        JLabel titleLabel = new JLabel("Set New Password", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(0, 122, 204));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JPanel formPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel newPassLabel = new JLabel("New Password:");
        newPassLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        newPasswordField = new JPasswordField();
        newPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        newPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel confirmLabel = new JLabel("Confirm Password:");
        confirmLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel reqLabel = new JLabel("<html><i>Minimum 4 characters required</i></html>");
        reqLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        reqLabel.setForeground(Color.GRAY);

        formPanel.add(newPassLabel);
        formPanel.add(newPasswordField);
        formPanel.add(confirmLabel);
        formPanel.add(confirmPasswordField);
        formPanel.add(reqLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);

        JButton resetBtn = createButton("Reset Password", new Color(0, 150, 0));
        JButton cancelBtn = createButton("Cancel", new Color(108, 117, 125));

        resetBtn.addActionListener(e -> resetPassword());
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(resetBtn);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ============= LOGIC =============

    private void sendOTP() {
        String phone = phoneField.getText().trim();

        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter phone number!");
            return;
        }

        if (!phone.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Phone number must be 10 digits!");
            return;
        }

        try {
            String table = userType.equals("Parent") ? "Parents" : "Health_Workers";
            String contactCol = userType.equals("Parent") ? "P_Contact_No" : "H_Contact_No";
            String emailCol = userType.equals("Parent") ? "E_mail" : "H_Email";

            String query = "SELECT " + emailCol + " FROM " + table + " WHERE " + contactCol + " = ?";

            System.out.println("🔍 Executing query: " + query);
            System.out.println("📞 Phone: " + phone);

            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, phone);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this,
                        "❌ Phone number not found!\n\nPlease check and try again.",
                        "Not Found", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String email = rs.getString(emailCol);

            System.out.println("📧 Email found: " + email);

            if (email == null || email.trim().isEmpty() || !email.contains("@")) {
                JOptionPane.showMessageDialog(this,
                        "❌ No email registered!\n\n" +
                                "Password recovery requires email.\n\n" +
                                "Please contact administrator:\n" +
                                "📞 Health Hotline: 1115 (toll-free)",
                        "No Email", JOptionPane.ERROR_MESSAGE);
                return;
            }

            verifiedPhone = phone;
            verifiedEmail = email;
            generatedOTP = String.format("%06d", new Random().nextInt(1000000));

            System.out.println("🔑 Generated OTP: " + generatedOTP);

            boolean emailSent = Notifications.sendEmail(
                    email,
                    "Password Reset OTP - Child Vaccination",
                    generateOTPEmail(generatedOTP)
            );

            if (emailSent) {
                JOptionPane.showMessageDialog(this,
                        "✅ OTP sent to: " + maskEmail(email) + "\n\n⏱️ Valid for 5 minutes",
                        "OTP Sent", JOptionPane.INFORMATION_MESSAGE);

                emailDisplayLabel.setText("📧 Sent to: " + maskEmail(email));
                startTimer();
                cardLayout.show(mainPanel, "STEP2");
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ Failed to send OTP!\n\nCheck internet connection.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error in sendOTP: " + e.getMessage());
            e.printStackTrace();

            String errorMsg = "Database Error!\n\n";
            if (e.getMessage().contains("H_Email") || e.getMessage().contains("Unknown column")) {
                errorMsg += "⚠️ Email column not found!\n\n" +
                        "Please run this SQL command:\n" +
                        "ALTER TABLE health_workers ADD COLUMN H_Email VARCHAR(100) AFTER H_Contact_No;\n\n" +
                        "Then try again.";
            } else {
                errorMsg += e.getMessage();
            }

            JOptionPane.showMessageDialog(this, errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resendOTP() {
        stopTimer();
        generatedOTP = String.format("%06d", new Random().nextInt(1000000));

        System.out.println("🔄 Resending OTP: " + generatedOTP);

        boolean sent = Notifications.sendEmail(
                verifiedEmail,
                "Password Reset OTP (Resent)",
                generateOTPEmail(generatedOTP)
        );

        if (sent) {
            JOptionPane.showMessageDialog(this, "✅ OTP resent!");
            timeRemaining = 300;
            startTimer();
        } else {
            JOptionPane.showMessageDialog(this, "❌ Failed to resend!");
        }
    }

    private void verifyOTP() {
        String entered = otpField.getText().trim();

        if (entered.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter OTP!");
            return;
        }

        System.out.println("🔍 Verifying OTP - Entered: " + entered + ", Expected: " + generatedOTP);

        if (entered.equals(generatedOTP)) {
            stopTimer();
            JOptionPane.showMessageDialog(this, "✅ OTP Verified!");
            cardLayout.show(mainPanel, "STEP3");
        } else {
            JOptionPane.showMessageDialog(this, "❌ Invalid OTP!");
            otpField.setText("");
        }
    }

    private void resetPassword() {
        String newPass = new String(newPasswordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());

        if (newPass.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }

        if (newPass.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password must be 4+ characters!");
            return;
        }

        if (!newPass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords don't match!");
            return;
        }

        try {
            String table = userType.equals("Parent") ? "Parents" : "Health_Workers";
            String contactCol = userType.equals("Parent") ? "P_Contact_No" : "H_Contact_No";

            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE " + table + " SET Password = ? WHERE " + contactCol + " = ?"
            );
            ps.setString(1, newPass);
            ps.setString(2, verifiedPhone);
            int updated = ps.executeUpdate();

            System.out.println("✅ Password updated for phone: " + verifiedPhone);

            if (updated > 0) {
                JOptionPane.showMessageDialog(this,
                        "✅ Password Reset Successful!\n\nYou can now login.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update!");
            }

        } catch (Exception e) {
            System.err.println("❌ Error resetting password: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void startTimer() {
        timeRemaining = 300;
        countdownTimer = new Timer(1000, e -> {
            timeRemaining--;
            int min = timeRemaining / 60;
            int sec = timeRemaining % 60;
            timerLabel.setText(String.format("⏱️ Time: %02d:%02d", min, sec));

            if (timeRemaining <= 60) {
                timerLabel.setForeground(new Color(220, 20, 60));
            }

            if (timeRemaining <= 0) {
                stopTimer();
                JOptionPane.showMessageDialog(this, "⏰ OTP Expired!");
                cardLayout.show(mainPanel, "STEP1");
            }
        });
        countdownTimer.start();
    }

    private void stopTimer() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];
        if (name.length() <= 3) return name.charAt(0) + "***@" + domain;
        return name.substring(0, 2) + "***" + name.charAt(name.length() - 1) + "@" + domain;
    }

    private String generateOTPEmail(String otp) {
        return String.format(
                "<html><body style='font-family: Arial; padding: 20px;'>" +
                        "<div style='max-width: 500px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px;'>" +
                        "<h2 style='color: #007acc; text-align: center;'>🔐 Password Reset</h2>" +
                        "<p>Your OTP code is:</p>" +
                        "<div style='background: #e3f2fd; padding: 20px; text-align: center; margin: 20px 0;'>" +
                        "<h1 style='color: #007acc; font-size: 36px; letter-spacing: 5px;'>%s</h1>" +
                        "</div>" +
                        "<p style='color: #666;'><b>Valid for 5 minutes</b></p>" +
                        "<p style='font-size: 12px; color: #999; text-align: center;'>Child Vaccination System</p>" +
                        "</div></body></html>",
                otp
        );
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 40));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(color);
            }
        });

        return btn;
    }

    @Override
    public void dispose() {
        stopTimer();
        try {
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (Exception ignored) {}
        super.dispose();
    }
}
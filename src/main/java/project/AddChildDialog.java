package project;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
public class AddChildDialog extends JDialog {
    private Connection conn;
    private JTextField parentNameField, parentPhoneField, parentEmailField, parentAddressField;
    private JTextField childNameField, childDobField, childAddressField;
    private JComboBox<String> childGenderCombo, childProvinceCombo;
    private JPasswordField parentPasswordField, confirmPasswordField;

    public AddChildDialog(JFrame parent, Connection conn) {
        super(parent, "Add Child & Parent Registration", true);
        this.conn = conn;

        setupUI();
        setVisible(true);
    }

    private void setupUI() {
        setSize(650, 850);
        setLocationRelativeTo(getParent());
        getContentPane().setBackground(Color.WHITE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(25, 35, 25, 35));
        mainPanel.setBackground(Color.WHITE);

        // Header
        JLabel titleLabel = new JLabel("Add Child & Parent", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 102, 204));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);

        // Parent Section
        formPanel.add(createSectionLabel("Parent Information"));
        formPanel.add(Box.createVerticalStrut(10));

        parentNameField = new JTextField();
        parentPhoneField = new JTextField();
        parentEmailField = new JTextField();
        parentAddressField = new JTextField();
        parentPasswordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();

        formPanel.add(createFieldPanel("Parent Name: *", parentNameField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Phone Number: *", parentPhoneField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Email: (Optional)", parentEmailField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Address:", parentAddressField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Password: *", parentPasswordField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Confirm Password: *", confirmPasswordField));

        formPanel.add(Box.createVerticalStrut(25));

        // Child Section
        formPanel.add(createSectionLabel("Child Information"));
        formPanel.add(Box.createVerticalStrut(10));

        childNameField = new JTextField();
        childDobField = new JTextField();
        childDobField.setText(""); // Empty field - user can enter ANY year
        childDobField.setToolTipText("Format: YYYY-MM-DD");
        childGenderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        childProvinceCombo = new JComboBox<>(new String[]{
                "Koshi", "Madhesh", "Bagmati", "Gandaki", "Lumbini", "Karnali", "Sudurpashchim"
        });
        childAddressField = new JTextField();

        formPanel.add(createFieldPanel("Child Name: *", childNameField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Date of Birth (YYYY-MM-DD): *", childDobField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Gender: *", childGenderCombo));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Province: *", childProvinceCombo));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Address:", childAddressField));

        // Scroll Pane
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));
        buttonPanel.setBackground(Color.WHITE);

        JButton registerBtn = createStyledButton("Register", new Color(0, 170, 0));
        JButton cancelBtn = createStyledButton("Cancel", new Color(220, 20, 60));

        registerBtn.addActionListener(e -> performRegistration());
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(registerBtn);
        buttonPanel.add(cancelBtn);

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setForeground(new Color(0, 102, 204));
        label.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 102, 204)));
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        return label;
    }

    private JPanel createFieldPanel(String labelText, JComponent field) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(600, 70));

        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(500, 55));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(60, 60, 60));
        label.setPreferredSize(new Dimension(200, 25));

        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(280, 38));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        panel.add(label, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        wrapper.add(panel, gbc);

        return wrapper;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 50));
        return btn;
    }

    private void performRegistration() {
        // Get all values
        String parentName = parentNameField.getText().trim();
        String parentPhone = parentPhoneField.getText().trim();
        String parentEmail = parentEmailField.getText().trim();
        String parentAddress = parentAddressField.getText().trim();
        String password = new String(parentPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        String childName = childNameField.getText().trim();
        String childDob = childDobField.getText().trim();
        String childGender = (String) childGenderCombo.getSelectedItem();
        String childProvince = (String) childProvinceCombo.getSelectedItem();
        String childAddress = childAddressField.getText().trim();

        // Validation
        if (parentName.isEmpty() || parentPhone.isEmpty() || password.isEmpty() ||
                childName.isEmpty() || childDob.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill all required fields marked with *",
                    "Missing Information", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                    "Passwords don't match!",
                    "Password Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length() < 4) {
            JOptionPane.showMessageDialog(this,
                    "Password must be at least 4 characters!",
                    "Weak Password", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!parentPhone.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this,
                    "Phone number must be 10 digits!",
                    "Invalid Phone", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!childDob.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this,
                    "Date of Birth must be in YYYY-MM-DD format!",
                    "Invalid Date", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            LocalDate birthDate = LocalDate.parse(childDob);
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

            // Optional: Warn if child is very old
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
            conn.setAutoCommit(false);

            // Check if phone number already exists
            PreparedStatement checkPs = conn.prepareStatement(
                    "SELECT COUNT(*) FROM Parents WHERE P_Contact_No = ?"
            );
            checkPs.setString(1, parentPhone);
            ResultSet checkRs = checkPs.executeQuery();

            if (checkRs.next() && checkRs.getInt(1) > 0) {
                conn.rollback();
                JOptionPane.showMessageDialog(this,
                        "This phone number is already registered!\nPlease use a different number or login with existing account.",
                        "Duplicate Phone Number", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Insert Parent (Unique_Parent_ID will be auto-generated by trigger)
            PreparedStatement parentPs = conn.prepareStatement(
                    "INSERT INTO Parents (P_Name, P_Contact_No, E_mail, Address, Password) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            parentPs.setString(1, parentName);
            parentPs.setString(2, parentPhone);
            parentPs.setString(3, parentEmail.isEmpty() ? null : parentEmail);
            parentPs.setString(4, parentAddress.isEmpty() ? null : parentAddress);
            parentPs.setString(5, password);
            parentPs.executeUpdate();

            ResultSet keys = parentPs.getGeneratedKeys();
            if (!keys.next()) {
                throw new SQLException("Failed to get Parent ID");
            }
            int parentId = keys.getInt(1);


            PreparedStatement getUniqueId = conn.prepareStatement(
                    "SELECT Unique_Parent_ID FROM Parents WHERE Parent_Id = ?"
            );
            getUniqueId.setInt(1, parentId);
            ResultSet uniqueRs = getUniqueId.executeQuery();
            String uniqueParentId = uniqueRs.next() ? uniqueRs.getString(1) : "N/A";

            // Insert Child
            PreparedStatement childPs = conn.prepareStatement(
                    "INSERT INTO Children (Parent_Id, C_Name, Gender, Date_of_Birth, Province, Address) VALUES (?, ?, ?, ?, ?, ?)"
            );
            childPs.setInt(1, parentId);
            childPs.setString(2, childName);
            childPs.setString(3, childGender);
            childPs.setString(4, childDob);
            childPs.setString(5, childProvince);
            childPs.setString(6, childAddress.isEmpty() ? null : childAddress);
            childPs.executeUpdate();

            conn.commit();

            // ✅ SHOW UNIQUE PARENT ID IN SUCCESS MESSAGE
            JOptionPane.showMessageDialog(this,
                    "✅ Registration Successful!\n\n" +
                            "📋 Unique Parent ID: " + uniqueParentId + "\n" +
                            "👤 Parent: " + parentName + "\n" +
                            "📞 Phone: " + parentPhone + "\n" +
                            "👶 Child: " + childName + "\n\n" +
                            "The parent can now login using their phone number and password.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (SQLException ex) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {}

            JOptionPane.showMessageDialog(this,
                    "Registration Failed!\n\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {}
        }
    }
}
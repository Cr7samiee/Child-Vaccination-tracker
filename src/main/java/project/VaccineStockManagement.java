package project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class VaccineStockManagement extends JDialog {
    private Connection conn;
    private JTable stockTable;
    private DefaultTableModel model;

    public VaccineStockManagement(JFrame parent, Connection conn) {
        super(parent, "💉 Vaccine Stock Management", true);
        this.conn = conn;

        setupUI();
        createTableIfNotExists();
        loadStockData();

        setVisible(true);
    }

    private void createTableIfNotExists() {
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS vaccine_stock (
                    Stock_Id INT PRIMARY KEY AUTO_INCREMENT,
                    Vaccine_Name VARCHAR(100) NOT NULL,
                    Batch_Number VARCHAR(50) NOT NULL,
                    Quantity INT NOT NULL,
                    Expiry_Date DATE NOT NULL,
                    Supplier VARCHAR(100),
                    Last_Updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    UNIQUE KEY unique_batch (Vaccine_Name, Batch_Number)
                )
            """);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupUI() {
        setSize(1300, 750);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("Vaccine Stock Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(new Color(0, 150, 0));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 14));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> loadStockData());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);

        // TABLE
        String[] columns = {"ID", "Vaccine Name", "Batch No.", "Quantity", "Expiry Date",
                "Days to Expiry", "Status", "Supplier", "Last Updated"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        stockTable = new JTable(model);
        stockTable.setRowHeight(40);
        stockTable.setFont(new Font("Arial", Font.PLAIN, 13));
        stockTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        stockTable.getTableHeader().setBackground(new Color(0, 102, 204));
        stockTable.getTableHeader().setForeground(Color.WHITE);

        // Custom renderer for status colors
        stockTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    String status = (String) table.getValueAt(row, 6);
                    switch (status) {
                        case "✅ Good Stock" -> c.setBackground(new Color(230, 255, 230));
                        case "⚠️ Low Stock" -> c.setBackground(new Color(255, 250, 205));
                        case "❌ Out of Stock" -> c.setBackground(new Color(255, 230, 230));
                        case "⏰ Expiring Soon" -> c.setBackground(new Color(255, 240, 200));
                        case "☠️ Expired" -> c.setBackground(new Color(255, 200, 200));
                        default -> c.setBackground(Color.WHITE);
                    }
                } else {
                    c.setBackground(new Color(100, 200, 255));
                }

                if (column == 6) {
                    setFont(new Font("Arial", Font.BOLD, 12));
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(stockTable);

        // BUTTON PANEL
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonPanel.setBackground(Color.WHITE);

        JButton addBtn = createButton("Add Stock", new Color(0, 170, 0));
        JButton updateBtn = createButton("Update Stock", new Color(0, 123, 255));
        JButton deleteBtn = createButton("Delete Stock", new Color(220, 53, 69));
        JButton alertsBtn = createButton("View Alerts", new Color(255, 140, 0));
        JButton reportBtn = createButton("Generate Report", new Color(138, 43, 226));

        addBtn.addActionListener(e -> showAddStockDialog());
        updateBtn.addActionListener(e -> showUpdateStockDialog());
        deleteBtn.addActionListener(e -> deleteStock());
        alertsBtn.addActionListener(e -> showAlerts());
        reportBtn.addActionListener(e -> generateStockReport());

        buttonPanel.add(addBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(alertsBtn);
        buttonPanel.add(reportBtn);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return btn;
    }

    private void loadStockData() {
        model.setRowCount(0);
        try {
            String query = "SELECT * FROM vaccine_stock ORDER BY Vaccine_Name, Expiry_Date";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                int id = rs.getInt("Stock_Id");
                String vaccine = rs.getString("Vaccine_Name");
                String batch = rs.getString("Batch_Number");
                int quantity = rs.getInt("Quantity");
                LocalDate expiry = rs.getDate("Expiry_Date").toLocalDate();
                String supplier = rs.getString("Supplier");
                Timestamp updated = rs.getTimestamp("Last_Updated");

                long daysToExpiry = ChronoUnit.DAYS.between(LocalDate.now(), expiry);
                String status = getStatus(quantity, daysToExpiry);

                model.addRow(new Object[]{
                        id, vaccine, batch, quantity, expiry,
                        daysToExpiry + " days", status, supplier, updated
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading stock: " + e.getMessage());
        }
    }

    private String getStatus(int quantity, long daysToExpiry) {
        if (daysToExpiry < 0) return "☠️ Expired";
        if (daysToExpiry <= 30) return "⏰ Expiring Soon";
        if (quantity == 0) return "❌ Out of Stock";
        if (quantity < 50) return "⚠️ Low Stock";
        return "✅ Good Stock";
    }

    private void showAddStockDialog() {
        JDialog dialog = new JDialog(this, "Add New Stock", true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(0, 2, 15, 15));
        dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] vaccines = {"BCG", "DPT-HepB-Hib", "Oral Polio Vaccine (OPV)", "fIPV",
                "Rota Virus", "PCV", "Measles-Rubella (MR)", "TCV",
                "Japanese Encephalitis (JE)", "HPV", "Td"};

        JComboBox<String> vaccineCombo = new JComboBox<>(vaccines);
        JTextField batchField = new JTextField();
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(100, 0, 10000, 10));
        JTextField expiryField = new JTextField(LocalDate.now().plusYears(2).toString());
        JTextField supplierField = new JTextField();

        dialog.add(new JLabel("Vaccine Name:"));
        dialog.add(vaccineCombo);
        dialog.add(new JLabel("Batch Number:"));
        dialog.add(batchField);
        dialog.add(new JLabel("Quantity:"));
        dialog.add(quantitySpinner);
        dialog.add(new JLabel("Expiry Date (YYYY-MM-DD):"));
        dialog.add(expiryField);
        dialog.add(new JLabel("Supplier:"));
        dialog.add(supplierField);

        JButton saveBtn = new JButton("💾 Save");
        JButton cancelBtn = new JButton("❌ Cancel");

        saveBtn.addActionListener(e -> {
            try {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO vaccine_stock (Vaccine_Name, Batch_Number, Quantity, Expiry_Date, Supplier) " +
                                "VALUES (?, ?, ?, ?, ?)"
                );
                ps.setString(1, (String) vaccineCombo.getSelectedItem());
                ps.setString(2, batchField.getText().trim());
                ps.setInt(3, (int) quantitySpinner.getValue());
                ps.setString(4, expiryField.getText().trim());
                ps.setString(5, supplierField.getText().trim());
                ps.executeUpdate();

                JOptionPane.showMessageDialog(dialog, "✅ Stock added successfully!");
                dialog.dispose();
                loadStockData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "❌ Error: " + ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(saveBtn);
        dialog.add(cancelBtn);
        dialog.setVisible(true);
    }

    private void showUpdateStockDialog() {
        int row = stockTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a stock entry!");
            return;
        }

        int stockId = (int) model.getValueAt(row, 0);
        String currentVaccine = (String) model.getValueAt(row, 1);
        String currentBatch = (String) model.getValueAt(row, 2);
        int currentQuantity = (int) model.getValueAt(row, 3);

        JDialog dialog = new JDialog(this, "Update Stock", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(0, 2, 15, 15));
        dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel infoLabel = new JLabel("<html><b>" + currentVaccine + "</b><br>Batch: " + currentBatch + "</html>");
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(currentQuantity, 0, 10000, 10));

        dialog.add(new JLabel("Stock Info:"));
        dialog.add(infoLabel);
        dialog.add(new JLabel("New Quantity:"));
        dialog.add(quantitySpinner);

        JButton saveBtn = new JButton("💾 Update");
        JButton cancelBtn = new JButton("❌ Cancel");

        saveBtn.addActionListener(e -> {
            try {
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE vaccine_stock SET Quantity = ? WHERE Stock_Id = ?"
                );
                ps.setInt(1, (int) quantitySpinner.getValue());
                ps.setInt(2, stockId);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(dialog, "✅ Stock updated successfully!");
                dialog.dispose();
                loadStockData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "❌ Error: " + ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(saveBtn);
        dialog.add(cancelBtn);
        dialog.setVisible(true);
    }

    private void deleteStock() {
        int row = stockTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a stock entry!");
            return;
        }

        int stockId = (int) model.getValueAt(row, 0);
        String vaccine = (String) model.getValueAt(row, 1);
        String batch = (String) model.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete stock entry?\n\nVaccine: " + vaccine + "\nBatch: " + batch,
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM vaccine_stock WHERE Stock_Id = ?");
                ps.setInt(1, stockId);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "✅ Stock entry deleted!");
                loadStockData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "❌ Error: " + e.getMessage());
            }
        }
    }

    private void showAlerts() {
        StringBuilder alerts = new StringBuilder("<html><body style='width: 400px; font-family: Arial;'>");
        alerts.append("<h2>🔔 Stock Alerts</h2>");

        try {
            // Expired vaccines
            PreparedStatement ps1 = conn.prepareStatement(
                    "SELECT Vaccine_Name, Batch_Number, Quantity FROM vaccine_stock WHERE Expiry_Date < CURDATE()"
            );
            ResultSet rs1 = ps1.executeQuery();

            alerts.append("<h3 style='color: red;'>☠️ Expired Vaccines:</h3><ul>");
            boolean hasExpired = false;
            while (rs1.next()) {
                hasExpired = true;
                alerts.append("<li>").append(rs1.getString(1))
                        .append(" (").append(rs1.getString(2))
                        .append(") - Qty: ").append(rs1.getInt(3)).append("</li>");
            }
            if (!hasExpired) alerts.append("<li>None</li>");
            alerts.append("</ul>");

            // Expiring soon
            PreparedStatement ps2 = conn.prepareStatement(
                    "SELECT Vaccine_Name, Batch_Number, Quantity, Expiry_Date " +
                            "FROM vaccine_stock WHERE Expiry_Date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY)"
            );
            ResultSet rs2 = ps2.executeQuery();

            alerts.append("<h3 style='color: orange;'>⏰ Expiring in 30 Days:</h3><ul>");
            boolean hasExpiring = false;
            while (rs2.next()) {
                hasExpiring = true;
                alerts.append("<li>").append(rs2.getString(1))
                        .append(" (").append(rs2.getString(2))
                        .append(") - Expires: ").append(rs2.getDate(4)).append("</li>");
            }
            if (!hasExpiring) alerts.append("<li>None</li>");
            alerts.append("</ul>");

            // Low stock
            PreparedStatement ps3 = conn.prepareStatement(
                    "SELECT Vaccine_Name, Batch_Number, Quantity FROM vaccine_stock WHERE Quantity < 50 AND Quantity > 0"
            );
            ResultSet rs3 = ps3.executeQuery();

            alerts.append("<h3 style='color: #FF8C00;'>⚠️ Low Stock (< 50):</h3><ul>");
            boolean hasLow = false;
            while (rs3.next()) {
                hasLow = true;
                alerts.append("<li>").append(rs3.getString(1))
                        .append(" (").append(rs3.getString(2))
                        .append(") - Qty: ").append(rs3.getInt(3)).append("</li>");
            }
            if (!hasLow) alerts.append("<li>None</li>");
            alerts.append("</ul>");

            // Out of stock
            PreparedStatement ps4 = conn.prepareStatement(
                    "SELECT Vaccine_Name, Batch_Number FROM vaccine_stock WHERE Quantity = 0"
            );
            ResultSet rs4 = ps4.executeQuery();

            alerts.append("<h3 style='color: red;'>❌ Out of Stock:</h3><ul>");
            boolean hasOut = false;
            while (rs4.next()) {
                hasOut = true;
                alerts.append("<li>").append(rs4.getString(1))
                        .append(" (").append(rs4.getString(2)).append(")</li>");
            }
            if (!hasOut) alerts.append("<li>None</li>");
            alerts.append("</ul>");

        } catch (SQLException e) {
            alerts.append("<p style='color: red;'>Error loading alerts: ").append(e.getMessage()).append("</p>");
        }

        alerts.append("</body></html>");

        JOptionPane.showMessageDialog(this,
                new JLabel(alerts.toString()),
                "Stock Alerts",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void generateStockReport() {
        try {
            VaccineStockReportGenerator generator = new VaccineStockReportGenerator(conn);
            String fileName = generator.generateReport();

            int choice = JOptionPane.showConfirmDialog(this,
                    "✅ Report generated!\n\n" + fileName + "\n\nOpen now?",
                    "Success", JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                Desktop.getDesktop().open(new java.io.File(fileName));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + e.getMessage());
        }
    }
}
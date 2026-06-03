package project;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class DashboardStatisticsPanel extends JPanel {
    private Connection conn;
    private JLabel totalChildrenLabel, todayApptsLabel, monthlyApptsLabel;
    private JLabel overdueLabel, lowStockLabel, fullyVaccinatedLabel;

    public DashboardStatisticsPanel(Connection conn) {
        this.conn = conn;
        setupUI();
        loadStatistics();

        // Auto-refresh every 30 seconds
        Timer refreshTimer = new Timer(30000, e -> loadStatistics());
        refreshTimer.start();
    }

    private void setupUI() {
        setLayout(new GridLayout(2, 3, 15, 15));
        setBackground(new Color(240, 248, 255));
        setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        // Create stat cards
        add(createStatCard("Total Children", "0", new Color(33, 150, 243),
                stat -> totalChildrenLabel = stat));
        add(createStatCard("Today's Appointments", "0", new Color(76, 175, 80),
                stat -> todayApptsLabel = stat));
        add(createStatCard("This Month", "0", new Color(156, 39, 176),
                stat -> monthlyApptsLabel = stat));
        add(createStatCard("Overdue", "0", new Color(244, 67, 54),
                stat -> overdueLabel = stat));
        add(createStatCard("Low Stock Vaccines", "0", new Color(255, 152, 0),
                stat -> lowStockLabel = stat));
        add(createStatCard("Fully Vaccinated", "0%", new Color(0, 150, 136),
                stat -> fullyVaccinatedLabel = stat));
    }

    private JPanel createStatCard(String title, String initialValue, Color color,
                                  java.util.function.Consumer<JLabel> labelSetter) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow
                g2d.setColor(new Color(0, 0, 0, 15));
                g2d.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 15, 15);

                // Card background
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, 15, 15);

                // Top accent bar
                g2d.setColor(color);
                g2d.fillRoundRect(0, 0, getWidth() - 6, 5, 15, 15);
            }
        };

        card.setLayout(new BorderLayout(8, 8));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        card.setPreferredSize(new Dimension(180, 90));
        card.setMaximumSize(new Dimension(200, 100));

        // Title
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(new Color(60, 60, 60));

        // Value
        JLabel valueLabel = new JLabel(initialValue, JLabel.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(color);
        labelSetter.accept(valueLabel);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    public void loadStatistics() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private int totalChildren, todayAppts, monthlyAppts, overdue, lowStock;
            private double fullyVaccinated;

            @Override
            protected Void doInBackground() throws Exception {
                // Total Children
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM Children")) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) totalChildren = rs.getInt(1);
                }

                // Today's Appointments
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM Appointments WHERE Appointment_Date = CURDATE() " +
                                "AND Status = 'Scheduled'")) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) todayAppts = rs.getInt(1);
                }

                // This Month's Appointments
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM Appointments WHERE YEAR(Appointment_Date) = YEAR(CURDATE()) " +
                                "AND MONTH(Appointment_Date) = MONTH(CURDATE()) AND Status = 'Scheduled'")) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) monthlyAppts = rs.getInt(1);
                }

                // Overdue Appointments
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM Appointments WHERE Appointment_Date < CURDATE() " +
                                "AND Status = 'Scheduled'")) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) overdue = rs.getInt(1);
                }

                // Low Stock Vaccines
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM vaccine_stock WHERE Quantity < 50 AND Quantity > 0")) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) lowStock = rs.getInt(1);
                } catch (SQLException e) {
                    // Table might not exist
                    lowStock = 0;
                }

                // Fully Vaccinated Children (11 required vaccines)
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT (COUNT(DISTINCT CASE WHEN completed >= 11 THEN Child_Id END) * 100.0 / " +
                                "NULLIF(COUNT(DISTINCT c.Child_Id), 0)) as percentage FROM Children c " +
                                "LEFT JOIN (SELECT Child_Id, COUNT(DISTINCT Vaccine_Name) as completed " +
                                "FROM Appointments WHERE Status = 'Completed' GROUP BY Child_Id) a " +
                                "ON c.Child_Id = a.Child_Id")) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) fullyVaccinated = rs.getDouble(1);
                }

                return null;
            }

            @Override
            protected void done() {
                totalChildrenLabel.setText(String.valueOf(totalChildren));
                todayApptsLabel.setText(String.valueOf(todayAppts));
                monthlyApptsLabel.setText(String.valueOf(monthlyAppts));
                overdueLabel.setText(String.valueOf(overdue));
                lowStockLabel.setText(String.valueOf(lowStock));
                fullyVaccinatedLabel.setText(String.format("%.1f%%", fullyVaccinated));
            }
        };

        worker.execute();
    }
}
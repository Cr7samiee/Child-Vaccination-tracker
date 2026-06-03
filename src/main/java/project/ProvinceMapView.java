package project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ProvinceMapView extends JDialog {
    private Connection conn;
    private Map<String, ProvinceData> provinceStats = new HashMap<>();
    private String selectedProvince = null;

    public ProvinceMapView(JFrame parent, Connection conn) {
        super(parent, "🗺️ Nepal Province Map - Vaccination Coverage", true);
        this.conn = conn;
        loadProvinceData();
        setupUI();
        setVisible(true);
    }

    private void loadProvinceData() {
        String[] provinces = {"Koshi", "Madhesh", "Bagmati", "Gandaki", "Lumbini", "Karnali", "Sudurpashchim"};

        for (String province : provinces) {
            try {
                // Get total children
                PreparedStatement ps1 = conn.prepareStatement(
                        "SELECT COUNT(*) FROM Children WHERE Province = ?"
                );
                ps1.setString(1, province);
                ResultSet rs1 = ps1.executeQuery();
                int totalChildren = rs1.next() ? rs1.getInt(1) : 0;

                // Get fully vaccinated children
                PreparedStatement ps2 = conn.prepareStatement(
                        "SELECT COUNT(DISTINCT c.Child_Id) FROM Children c " +
                                "JOIN (SELECT Child_Id FROM Appointments WHERE Status = 'Completed' " +
                                "GROUP BY Child_Id HAVING COUNT(DISTINCT Vaccine_Name) >= 11) a " +
                                "ON c.Child_Id = a.Child_Id WHERE c.Province = ?"
                );
                ps2.setString(1, province);
                ResultSet rs2 = ps2.executeQuery();
                int fullyVaccinated = rs2.next() ? rs2.getInt(1) : 0;

                double coverage = totalChildren > 0 ? (fullyVaccinated * 100.0 / totalChildren) : 0;

                provinceStats.put(province, new ProvinceData(totalChildren, fullyVaccinated, coverage));

            } catch (Exception e) {
                provinceStats.put(province, new ProvinceData(0, 0, 0));
            }
        }
    }

    private void setupUI() {
        setSize(1200, 800);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("Nepal Vaccination Coverage by Province");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(Color.WHITE);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(new Color(0, 170, 0));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 14));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> {
            loadProvinceData();
            repaint();
        });

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);

        // Map Panel
        MapPanel mapPanel = new MapPanel();

        // Details Panel
        JPanel detailsPanel = new JPanel();
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        detailsPanel.setPreferredSize(new Dimension(350, 0));

        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        detailsArea.setText(generateSummary());

        JScrollPane scrollPane = new JScrollPane(detailsArea);
        detailsPanel.setLayout(new BorderLayout());
        detailsPanel.add(new JLabel("📊 Province Statistics", JLabel.CENTER) {{
            setFont(new Font("Arial", Font.BOLD, 18));
            setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        }}, BorderLayout.NORTH);
        detailsPanel.add(scrollPane, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(mapPanel, BorderLayout.CENTER);
        add(detailsPanel, BorderLayout.EAST);
    }

    private String generateSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("VACCINATION COVERAGE REPORT\n");
        sb.append("=" .repeat(35)).append("\n\n");

        for (Map.Entry<String, ProvinceData> entry : provinceStats.entrySet()) {
            ProvinceData data = entry.getValue();
            sb.append(String.format("%-15s\n", entry.getKey()));
            sb.append(String.format("  Children: %d\n", data.totalChildren));
            sb.append(String.format("  Vaccinated: %d\n", data.fullyVaccinated));
            sb.append(String.format("  Coverage: %.1f%%\n", data.coverage));
            sb.append("\n");
        }

        return sb.toString();
    }

    class MapPanel extends JPanel {
        public MapPanel() {
            setBackground(new Color(245, 250, 255));

            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    String province = getProvinceAtPoint(e.getPoint());
                    if (province != null && !province.equals(selectedProvince)) {
                        selectedProvince = province;
                        repaint();
                        setCursor(new Cursor(Cursor.HAND_CURSOR));
                    } else if (province == null) {
                        selectedProvince = null;
                        repaint();
                        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    String province = getProvinceAtPoint(e.getPoint());
                    if (province != null) {
                        showProvinceDetails(province);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Draw title
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.setColor(new Color(60, 60, 60));
            g2d.drawString("Click on a province to view detailed statistics", 50, 30);

            // Draw simplified Nepal map (7 provinces as rectangles in approximate positions)
            int startY = 80;
            int provinceHeight = 90;
            int spacing = 15;  // Increased from 10 to 15

            String[] provinces = {"Koshi", "Madhesh", "Bagmati", "Gandaki", "Lumbini", "Karnali", "Sudurpashchim"};

            for (int i = 0; i < provinces.length; i++) {
                String province = provinces[i];
                ProvinceData data = provinceStats.get(province);

                int row = i / 3;
                int col = i % 3;

                int x = 50 + (col * 290);  // Horizontal spacing: 290px
                int y = startY + (row * (provinceHeight + spacing));

                // Determine color based on coverage
                Color baseColor = getCoverageColor(data.coverage);
                if (province.equals(selectedProvince)) {
                    baseColor = baseColor.brighter();
                }

                // Draw province box
                g2d.setColor(baseColor);
                g2d.fillRoundRect(x, y, 220, provinceHeight, 15, 15);

                // Draw border
                g2d.setColor(new Color(100, 100, 100));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(x, y, 220, provinceHeight, 15, 15);

                // Draw text
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 18));
                g2d.drawString(province, x + 15, y + 30);

                g2d.setFont(new Font("Arial", Font.PLAIN, 14));
                g2d.drawString("Children: " + data.totalChildren, x + 15, y + 55);
                g2d.drawString(String.format("Coverage: %.1f%%", data.coverage), x + 15, y + 75);
            }

            // Draw legend
            int legendY = startY + (3 * (provinceHeight + spacing)) + 30;
            drawLegend(g2d, 50, legendY);  // Left-aligned below provinces
        }

        private void drawLegend(Graphics2D g2d, int x, int y) {
            g2d.setColor(new Color(250, 250, 250));
            g2d.fillRoundRect(x, y, 200, 120, 10, 10);
            g2d.setColor(Color.GRAY);
            g2d.drawRoundRect(x, y, 200, 120, 10, 10);

            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString("Coverage Legend:", x + 10, y + 20);

            String[] labels = {"< 25% - Poor", "25-50% - Low", "50-75% - Good", "> 75% - Excellent"};
            Color[] colors = {
                    new Color(220, 53, 69),
                    new Color(255, 152, 0),
                    new Color(255, 235, 59),
                    new Color(76, 175, 80)
            };

            for (int i = 0; i < labels.length; i++) {
                g2d.setColor(colors[i]);
                g2d.fillRect(x + 10, y + 35 + (i * 20), 15, 15);
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                g2d.drawString(labels[i], x + 30, y + 47 + (i * 20));
            }
        }

        private Color getCoverageColor(double coverage) {
            if (coverage >= 75) return new Color(76, 175, 80);      // Green
            if (coverage >= 50) return new Color(255, 235, 59);     // Yellow
            if (coverage >= 25) return new Color(255, 152, 0);      // Orange
            return new Color(220, 53, 69);                          // Red
        }

        private String getProvinceAtPoint(Point p) {
            int startY = 80;
            int provinceHeight = 90;
            int spacing = 15;  // Match the spacing above

            String[] provinces = {"Koshi", "Madhesh", "Bagmati", "Gandaki", "Lumbini", "Karnali", "Sudurpashchim"};

            for (int i = 0; i < provinces.length; i++) {
                // ✅ FIXED: Match the grid layout
                int row = i / 3;
                int col = i % 3;

                int x = 50 + (col * 290);  // Match 290px spacing
                int y = startY + (row * (provinceHeight + spacing));

                if (p.x >= x && p.x <= x + 220 && p.y >= y && p.y <= y + provinceHeight) {
                    return provinces[i];
                }
            }
            return null;
        }
    }

    private void showProvinceDetails(String province) {
        ProvinceData data = provinceStats.get(province);

        String message = String.format(
                "<html><body style='font-size: 12px;'>" +
                        "<h2 style='color: #0066CC;'>%s Province</h2>" +
                        "<table style='border-collapse: collapse; width: 100%%;'>" +
                        "<tr><td><b>Total Children:</b></td><td>%d</td></tr>" +
                        "<tr><td><b>Fully Vaccinated:</b></td><td>%d</td></tr>" +
                        "<tr><td><b>Partially Vaccinated:</b></td><td>%d</td></tr>" +
                        "<tr><td><b>Coverage:</b></td><td style='color: %s;'><b>%.1f%%</b></td></tr>" +
                        "</table>" +
                        "</body></html>",
                province,
                data.totalChildren,
                data.fullyVaccinated,
                data.totalChildren - data.fullyVaccinated,
                data.coverage >= 50 ? "green" : "red",
                data.coverage
        );

        JOptionPane.showMessageDialog(this, new JLabel(message),
                province + " Statistics", JOptionPane.INFORMATION_MESSAGE);
    }

    static class ProvinceData {
        int totalChildren;
        int fullyVaccinated;
        double coverage;

        ProvinceData(int total, int vaccinated, double coverage) {
            this.totalChildren = total;
            this.fullyVaccinated = vaccinated;
            this.coverage = coverage;
        }
    }
}
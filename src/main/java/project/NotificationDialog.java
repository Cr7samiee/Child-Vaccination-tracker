package project;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class NotificationDialog extends JDialog {
    private Connection conn;
    private JTable appointmentsTable;
    private JTextArea msgArea;
    private JCheckBox smsBox, emailBox;

    public NotificationDialog(JFrame parent, Connection conn, JTable appointmentsTable) {
        super(parent, "Send Reminder to Selected Children", true);
        this.conn = conn;
        this.appointmentsTable = appointmentsTable;

        setSize(700, 600);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Color.WHITE);

        // Check if anything is selected
        if (appointmentsTable == null || appointmentsTable.getSelectedRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select at least one child from Upcoming Appointments!",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }

        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        main.setBackground(Color.WHITE);

        JLabel title = new JLabel("Send Reminder to Selected Parents", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(new Color(0, 102, 204));

        // Show how many selected
        int count = appointmentsTable.getSelectedRowCount();
        JLabel countLabel = new JLabel("Selected: " + count + " child(ren)", JLabel.CENTER);
        countLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        countLabel.setForeground(new Color(0, 120, 0));

        // Message area
        msgArea = new JTextArea(10, 50);
        msgArea.setText("Dear Parent,\n\nThis is a reminder for your child has a vaccination appointment soon.\nPlease come on time.\n\nThank you!\nHealth Center");
        msgArea.setFont(new Font("Arial", Font.PLAIN, 16));
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(msgArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Message (use 'child' for name)"));

        // Options
        JPanel options = new JPanel(new FlowLayout(FlowLayout.LEFT));
        smsBox = new JCheckBox("Send SMS");
        emailBox = new JCheckBox("Send Email");
        smsBox.setSelected(true);
        emailBox.setSelected(true);
        smsBox.setFont(new Font("Arial", Font.BOLD, 16));
        emailBox.setFont(new Font("Arial", Font.BOLD, 16));
        options.add(smsBox);
        options.add(emailBox);

        // Send button
        JButton sendBtn = new JButton("SEND REMINDER TO " + count + " PARENT(S)");
        sendBtn.setBackground(new Color(0, 150, 0));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setFont(new Font("Arial", Font.BOLD, 22));
        sendBtn.setPreferredSize(new Dimension(500, 60));
        sendBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendBtn.addActionListener(e -> sendToSelected());

        main.add(title, BorderLayout.NORTH);
        main.add(countLabel, BorderLayout.PAGE_START);
        main.add(scroll, BorderLayout.CENTER);
        main.add(options, BorderLayout.SOUTH);

        JPanel bottom = new JPanel();
        bottom.setBackground(Color.WHITE);
        bottom.add(sendBtn);

        add(main, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        setVisible(true);
    }

    private void sendToSelected() {
        String message = msgArea.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please write a message!");
            return;
        }

        boolean sendSMS = smsBox.isSelected();
        boolean sendEmail = emailBox.isSelected();

        if (!sendSMS && !sendEmail) {
            JOptionPane.showMessageDialog(this, "Please select SMS or Email!");
            return;
        }

        int[] rows = appointmentsTable.getSelectedRows();
        int sentCount = 0;

        // We'll use final variables for lambda
        final String finalMessage = message;

        for (int i : rows) {
            String childName = (String) appointmentsTable.getValueAt(i, 1);
            String phone = (String) appointmentsTable.getValueAt(i, 5);
            String email = (String) appointmentsTable.getValueAt(i, 6);

            String personalizedMsg = finalMessage.replace("child", childName);

            if (sendSMS && phone != null && phone.matches("\\d{10,}")) {
                Notifications.sendSMS(phone, personalizedMsg);
                sentCount++;
            }
            if (sendEmail && email != null && email.contains("@")) {
                Notifications.sendEmail(email, "Vaccination Reminder", personalizedMsg.replace("\n", "<br>"));
                sentCount++;
            }
        }

        // Final variable for lambda
        final int finalSentCount = sentCount;

        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    "<html><h2>SUCCESS!</h2>" +
                            "<p>Reminder sent to <b>" + finalSentCount + "</b> parent(s)</p></html>",
                    "Sent", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });
    }
}
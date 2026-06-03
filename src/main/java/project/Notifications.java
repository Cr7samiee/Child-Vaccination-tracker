package project;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.Authenticator;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

public class Notifications {

    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing()
            .systemProperties()
            .load();

    // Configure these in .env or system environment variables.
    private static final String GMAIL = getConfig("GMAIL_ADDRESS");
    private static final String APP_PASS = getConfig("GMAIL_APP_PASSWORD");
    private static final String TEXTBEE_DEVICE_ID = getConfig("TEXTBEE_DEVICE_ID");
    private static final String TEXTBEE_API_KEY = getConfig("TEXTBEE_API_KEY");

    // Health post contact
    private static final String HEALTH_POST_HOTLINE = "1115";
    private static final String HEALTH_POST_EMAIL = "healthpost@nepal.gov.np";

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private static String getConfig(String key) {
        String value = System.getenv(key);
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        value = DOTENV.get(key);
        return value == null ? "" : value.trim();
    }

    public static boolean sendSMS(String to, String body) {
        try {
            String cleanNumber = formatNepalPhoneNumber(to);

            if (cleanNumber == null || cleanNumber.isEmpty()) {
                System.err.println("SMS sending failed: invalid phone number");
                return false;
            }

            if (TEXTBEE_DEVICE_ID == null || TEXTBEE_DEVICE_ID.trim().isEmpty()
                    || TEXTBEE_API_KEY == null || TEXTBEE_API_KEY.trim().isEmpty()
                    || TEXTBEE_DEVICE_ID.equals("YOUR_TEXTBEE_DEVICE_ID")
                    || TEXTBEE_API_KEY.equals("YOUR_TEXTBEE_API_KEY")) {
                System.err.println("SMS sending failed: TextBee credentials are not configured");
                return false;
            }

            String url = "https://api.textbee.dev/api/v1/gateway/devices/"
                    + TEXTBEE_DEVICE_ID
                    + "/send-sms";

            String jsonBody = "{"
                    + "\"recipients\":[\"" + escapeJson(cleanNumber) + "\"],"
                    + "\"message\":\"" + escapeJson(body) + "\""
                    + "}";

            System.out.println("Attempting to send SMS using TextBee to: " + cleanNumber);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", TEXTBEE_API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            int statusCode = response.statusCode();

            if (statusCode >= 200 && statusCode < 300) {
                System.out.println("SMS sent successfully using TextBee!");
                System.out.println("   Status Code: " + statusCode);
                System.out.println("   Response: " + response.body());
                return true;
            } else {
                System.err.println("TextBee SMS sending failed:");
                System.err.println("   Status Code: " + statusCode);
                System.err.println("   Response: " + response.body());
                return false;
            }

        } catch (Exception e) {
            System.err.println("SMS sending failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static String formatNepalPhoneNumber(String phone) {
        if (phone == null) {
            return null;
        }

        String cleanNumber = phone.trim().replaceAll("[^0-9+]", "");

        if (cleanNumber.isEmpty()) {
            return null;
        }

        if (cleanNumber.startsWith("+")) {
            return cleanNumber;
        }

        if (cleanNumber.startsWith("977")) {
            return "+" + cleanNumber;
        }

        if (cleanNumber.startsWith("0")) {
            return "+977" + cleanNumber.substring(1);
        }

        if (cleanNumber.length() == 10) {
            return "+977" + cleanNumber;
        }

        return cleanNumber;
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static boolean sendEmail(String to, String subject, String htmlBody) {
        if (to == null || to.trim().isEmpty() || !to.contains("@")) {
            System.out.println("Skipping email, invalid or empty address: " + to);
            return false;
        }

        if (GMAIL.isEmpty() || APP_PASS.isEmpty()) {
            System.err.println("Email sending failed: Gmail credentials are not configured");
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(GMAIL, APP_PASS);
            }
        });

        try {
            System.out.println("Attempting to send email to: " + to);

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(GMAIL, "Child Vaccination System"));
            message.setRecipients(jakarta.mail.Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(htmlBody, "text/html; charset=utf-8");

            Transport.send(message);

            System.out.println("Email sent successfully to: " + to);
            return true;

        } catch (MessagingException e) {
            System.err.println("Email sending failed:");
            System.err.println("   To: " + to);
            System.err.println("   Error: " + e.getMessage());
            e.printStackTrace();
            return false;

        } catch (Exception e) {
            System.err.println("Unexpected email error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static void sendBothNotifications(String phone, String email, String childName,
                                             String vaccine, String dose, String date, String time) {
        String smsBody = String.format(
                "VACCINATION REMINDER\n\n" +
                        "Child: %s\n" +
                        "Vaccine: %s %s\n" +
                        "Date: %s\n" +
                        "Time: %s\n\n" +
                        "Please arrive on time.\n" +
                        "- Child Vaccination System",
                childName, vaccine, dose, date, time
        );

        String emailBody = String.format(
                "<html>" +
                        "<body style='font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;'>" +
                        "<div style='max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px;'>" +
                        "<h2 style='color: #0066cc; text-align: center;'>Vaccination Reminder</h2>" +
                        "<div style='background: #e3f2fd; padding: 20px; border-radius: 8px;'>" +
                        "<p><strong>Child Name:</strong> %s</p>" +
                        "<p><strong>Vaccine:</strong> %s %s</p>" +
                        "<p><strong>Date:</strong> %s</p>" +
                        "<p><strong>Time:</strong> %s</p>" +
                        "</div>" +
                        "<p style='color: #666; text-align: center;'>Please arrive on time for your child's vaccination appointment.</p>" +
                        "<hr>" +
                        "<p style='font-size: 12px; color: #999; text-align: center;'>Child Vaccination System - Government of Nepal</p>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                childName, vaccine, dose, date, time
        );

        System.out.println("\n" + "=".repeat(60));
        System.out.println("SENDING REMINDER NOTIFICATIONS");
        System.out.println("=".repeat(60));

        boolean smsSuccess = sendSMS(phone, smsBody);
        boolean emailSuccess = false;

        if (email != null && !email.trim().isEmpty() && email.contains("@")) {
            emailSuccess = sendEmail(email, "Vaccination Reminder - " + childName, emailBody);
        } else {
            System.out.println("No valid email provided, skipping email notification");
        }

        System.out.println("\nNOTIFICATION SUMMARY:");
        System.out.println("   SMS: " + (smsSuccess ? "Sent" : "Failed"));
        System.out.println("   Email: " + (emailSuccess ? "Sent" : "Skipped or Failed"));
        System.out.println("=".repeat(60) + "\n");
    }

    public static void sendMissedNotification(String phone, String email, String childName,
                                              String vaccine, String dose) {
        String smsBody = String.format(
                "MISSED VACCINATION ALERT\n\n" +
                        "Child: %s\n" +
                        "Vaccine: %s %s\n\n" +
                        "This appointment was missed.\n" +
                        "Please contact your nearest health post immediately to reschedule.\n\n" +
                        "Health Hotline: %s (toll-free)\n" +
                        "Email: %s\n\n" +
                        "- Child Vaccination System",
                childName, vaccine, dose, HEALTH_POST_HOTLINE, HEALTH_POST_EMAIL
        );

        String emailBody = String.format(
                "<html>" +
                        "<body style='font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;'>" +
                        "<div style='max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px;'>" +
                        "<h2 style='color: #dc3545; text-align: center;'>Missed Vaccination Alert</h2>" +
                        "<div style='background: #fff3cd; padding: 20px; border-radius: 8px; border-left: 4px solid #dc3545;'>" +
                        "<p><strong>Child Name:</strong> %s</p>" +
                        "<p><strong>Vaccine:</strong> %s %s</p>" +
                        "</div>" +
                        "<div style='background: #f8d7da; padding: 20px; border-radius: 8px; margin-top: 20px;'>" +
                        "<p style='color: #721c24; font-weight: bold;'>This vaccination appointment was marked as missed.</p>" +
                        "<p style='color: #721c24;'>Please contact your nearest health post immediately to reschedule this important vaccination.</p>" +
                        "</div>" +
                        "<div style='background: #e3f2fd; padding: 20px; border-radius: 8px; margin-top: 20px;'>" +
                        "<h3 style='color: #0066cc;'>Contact Health Post:</h3>" +
                        "<p><strong>Health Hotline:</strong> %s (toll-free)</p>" +
                        "<p><strong>Email:</strong> <a href='mailto:%s'>%s</a></p>" +
                        "</div>" +
                        "<hr>" +
                        "<p style='font-size: 12px; color: #999; text-align: center;'>Child Vaccination System - Government of Nepal</p>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                childName, vaccine, dose, HEALTH_POST_HOTLINE, HEALTH_POST_EMAIL, HEALTH_POST_EMAIL
        );

        System.out.println("\nSENDING MISSED APPOINTMENT NOTIFICATION...");
        sendSMS(phone, smsBody);

        if (email != null && email.contains("@")) {
            sendEmail(email, "URGENT: Missed Vaccination - " + childName, emailBody);
        }
    }

    public static void sendCompletedNotification(String phone, String email, String childName,
                                                 String vaccine, String dose, String date) {
        String smsBody = String.format(
                "VACCINATION COMPLETED\n\n" +
                        "Congratulations!\n\n" +
                        "Child: %s\n" +
                        "Vaccine: %s %s\n" +
                        "Date: %s\n\n" +
                        "Thank you for keeping your child healthy!\n" +
                        "- Child Vaccination System",
                childName, vaccine, dose, date
        );

        String emailBody = String.format(
                "<html>" +
                        "<body style='font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;'>" +
                        "<div style='max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px;'>" +
                        "<h2 style='color: #28a745; text-align: center;'>Vaccination Completed</h2>" +
                        "<div style='background: #d4edda; padding: 20px; border-radius: 8px; border-left: 4px solid #28a745;'>" +
                        "<p><strong>Child Name:</strong> %s</p>" +
                        "<p><strong>Vaccine:</strong> %s %s</p>" +
                        "<p><strong>Date:</strong> %s</p>" +
                        "</div>" +
                        "<p style='color: #666;'>Congratulations! Your child has successfully received their vaccination.</p>" +
                        "<hr>" +
                        "<p style='font-size: 12px; color: #999; text-align: center;'>Child Vaccination System - Government of Nepal</p>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                childName, vaccine, dose, date
        );

        System.out.println("\nSENDING COMPLETION NOTIFICATION...");
        sendSMS(phone, smsBody);

        if (email != null && email.contains("@")) {
            sendEmail(email, "Vaccination Completed - " + childName, emailBody);
        }
    }
}

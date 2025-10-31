// ✅ Filepath: src/main/java/vn/iotstar/services/MailService.java
package vn.iotstar.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.InputStream;
import java.util.Properties;

/**
 * ✅ Phiên bản hoàn thiện của MailService:
 * - Ưu tiên đọc cấu hình từ file mail-config.properties (src/main/resources)
 * - Nếu không có, fallback sang System.getProperty(...)
 * - Tương thích Gmail SMTP thật và cũng chạy được local (fake config)
 */
public class MailService {

    private Properties loadMailConfig() {
        Properties props = new Properties();
        try (InputStream input = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("mail-config.properties")) {
            if (input != null) {
                props.load(input);
                System.out.println("[MailService] ✅ Đã load cấu hình từ mail-config.properties");
            } else {
                System.out.println("[MailService] ⚠️ Không tìm thấy mail-config.properties, dùng System properties mặc định.");
            }
        } catch (Exception e) {
            System.out.println("[MailService] ⚠️ Lỗi khi load mail-config.properties: " + e.getMessage());
        }
        return props;
    }

    private Session buildSession() {
        Properties fileProps = loadMailConfig();
        Properties props = new Properties();

        // Ưu tiên lấy từ file, nếu không có thì lấy từ System.getProperty()
        props.put("mail.smtp.host", fileProps.getProperty("mail.smtp.host", System.getProperty("mail.smtp.host", "smtp.example.com")));
        props.put("mail.smtp.port", fileProps.getProperty("mail.smtp.port", System.getProperty("mail.smtp.port", "587")));
        props.put("mail.smtp.auth", fileProps.getProperty("mail.smtp.auth", System.getProperty("mail.smtp.auth", "true")));
        props.put("mail.smtp.starttls.enable", fileProps.getProperty("mail.smtp.starttls.enable", System.getProperty("mail.smtp.starttls.enable", "true")));
        props.put("mail.debug", fileProps.getProperty("mail.debug", System.getProperty("mail.debug", "false")));

        final String user = fileProps.getProperty("mail.smtp.user", System.getProperty("mail.smtp.user", "no-reply@example.com"));
        final String pass = fileProps.getProperty("mail.smtp.pass", System.getProperty("mail.smtp.pass", "changeme"));

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });
    }

    public void sendOtpEmail(String to, String code, String subjectPrefix) {
        try {
            Properties config = loadMailConfig();
            String fromEmail = config.getProperty("mail.smtp.user", System.getProperty("mail.smtp.user", "no-reply@example.com"));

            Session session = buildSession();
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("[" + subjectPrefix + "] Mã OTP của bạn");

            String body = """
                    Xin chào,

                    Mã OTP của bạn là: %s
                    OTP có hiệu lực trong 10 phút. Vui lòng không chia sẻ cho bất kỳ ai.

                    Trân trọng.
                    """.formatted(code);

            message.setText(body);
            Transport.send(message);

            System.out.println("[MailService] ✅ Gửi email OTP thành công tới: " + to);

        } catch (Exception e) {
            throw new RuntimeException("Gửi email OTP thất bại: " + e.getMessage(), e);
        }
    }
}

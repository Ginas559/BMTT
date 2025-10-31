// filepath: src/main/java/vn/iotstar/utils/MailUtil.java
package vn.iotstar.utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/**
 * Gửi mail OTP. Nếu cấu hình SMTP thiếu sẽ LOG ra console thay vì ném lỗi.
 */
public class MailUtil {

    public static boolean send(String toEmail, String subject, String content) {
        String host = System.getProperty("mail.smtp.host", "smtp.gmail.com");
        String port = System.getProperty("mail.smtp.port", "587");
        String username = System.getProperty("mail.smtp.username"); // ví dụ: your@gmail.com
        String password = System.getProperty("mail.smtp.password"); // App Password 16 ký tự
        String from     = System.getProperty("mail.from", username);

        try {
            if (username == null || password == null) {
                System.out.println("[MailUtil] (FAKE) Send to " + toEmail + " | " + subject + " | " + content);
                return false; // coi như không gửi được, nhưng không crash flow
            }

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);

            Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("[MailUtil] Gửi mail thất bại, fallback console: " + content);
            return false;
        }
    }
}

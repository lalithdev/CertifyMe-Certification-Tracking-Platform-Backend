package com.certifyme.app.service;

import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Service to handle email operations.
 * Works with both Gmail (Local/Dev) and SendGrid (Prod) via JavaMailSender.
 */
@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:verify.certifyme@gmail.com}")
    private String fromEmail;

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String mailHost;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @PostConstruct
    public void logConfig() {
        log.info("[EmailService] Initialized with host: {} and sender: {}", mailHost, fromEmail);
    }

    public void sendOtpEmail(String toEmail, String otp) {
        log.info("[EmailService] Sending OTP to: {}", toEmail);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "CertifyMe Support");
            helper.setTo(toEmail);
            helper.setSubject("Your CertifyMe Verification Code");

            String htmlContent = generateHtmlTemplate(otp);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("[EmailService] Email sent successfully!");

        } catch (MailException e) {
            log.error("[EmailService] Mail delivery failed: {}", e.getMessage());
            throw new RuntimeException("Email service is currently unavailable. Please try again later.", e);
        } catch (Exception e) {
            log.error("[EmailService] Unexpected error: {}", e.getMessage());
            throw new RuntimeException("An unexpected error occurred while sending email.", e);
        }
    }

    private String generateHtmlTemplate(String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                    <div style="max-width: 600px; margin: auto; background: white; padding: 20px; border-radius: 10px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);">
                        <h2 style="color: #071926; text-align: center;">CertifyMe Verification</h2>
                        <p style="font-size: 16px; color: #333;">Your verification code is:</p>
                        <div style="text-align: center; margin: 30px 0;">
                            <span style="font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #ffffff; background: #000; padding: 10px 20px; border-radius: 5px;">%s</span>
                        </div>
                        <p style="font-size: 14px; color: #666; text-align: center;">This code is valid for 10 minutes.</p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                        <p style="font-size: 12px; color: #999; text-align: center;">If you didn't request this, please ignore this email.</p>
                    </div>
                </body>
                </html>
                """.formatted(otp);
    }
}
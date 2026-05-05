package com.certifyme.app.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Enterprise-grade email service using the official SendGrid v3 API.
 * NO SMTP. NO JavaMailSender. NO Gmail. Pure SendGrid REST API only.
 */
@Slf4j
@Service
public class EmailService {

    @Value("${app.sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${app.mail.from}")
    private String fromEmail;

    private SendGrid sendGrid;

    @PostConstruct
    public void init() {
        this.sendGrid = new SendGrid(sendGridApiKey);
        log.info("[EmailService] SendGrid API initialized. Verified sender: {}", fromEmail);
    }

    /**
     * Sends an OTP email to the specified recipient using SendGrid REST API.
     *
     * @param toEmail recipient email address
     * @param otp     the one-time password to include in the email
     * @throws EmailDeliveryException if the email cannot be sent
     */
    public void sendOtpEmail(String toEmail, String otp) {
        log.info("[EmailService] Preparing OTP email for: {}", toEmail);

        Email from = new Email(fromEmail, "CertifyMe Support");
        Email to = new Email(toEmail);
        String subject = "Your CertifyMe Verification Code";
        Content content = new Content("text/html", generateOtpTemplate(otp));

        Mail mail = new Mail(from, subject, to, content);

        try {
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            int statusCode = response.getStatusCode();
            log.info("[EmailService] SendGrid response → status={}, to={}", statusCode, toEmail);

            if (statusCode >= 200 && statusCode < 300) {
                log.info("[EmailService] ✅ Email delivered successfully to: {}", toEmail);
            } else {
                log.error("[EmailService] ❌ SendGrid rejected email → status={}, body={}", statusCode, response.getBody());
                throw new EmailDeliveryException(
                        "SendGrid returned status " + statusCode + ": " + response.getBody());
            }

        } catch (EmailDeliveryException e) {
            throw e; // Re-throw our custom exception as-is
        } catch (Exception e) {
            log.error("[EmailService] ❌ Failed to send email via SendGrid API: {}", e.getMessage(), e);
            throw new EmailDeliveryException("Failed to send email. Please try again later.", e);
        }
    }

    /**
     * Generates a branded HTML email template for OTP delivery.
     */
    private String generateOtpTemplate(String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: 'Segoe UI', Arial, sans-serif; background-color: #f0f2f5; padding: 0; margin: 0;">
                    <div style="max-width: 600px; margin: 40px auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 24px rgba(0,0,0,0.08);">
                        <div style="background: linear-gradient(135deg, #071926 0%%, #0f2d40 100%%); padding: 32px 24px; text-align: center;">
                            <h1 style="color: #ffffff; font-size: 24px; margin: 0; font-weight: 700; letter-spacing: 0.5px;">CertifyMe</h1>
                            <p style="color: rgba(255,255,255,0.7); margin: 8px 0 0; font-size: 14px;">Certification Tracking Platform</p>
                        </div>
                        <div style="padding: 40px 32px; text-align: center;">
                            <h2 style="color: #1a1a2e; font-size: 20px; margin: 0 0 12px; font-weight: 600;">Verification Code</h2>
                            <p style="color: #64748b; font-size: 15px; margin: 0 0 32px; line-height: 1.6;">
                                Use the code below to verify your identity. This code is valid for <strong>10 minutes</strong>.
                            </p>
                            <div style="display: inline-block; background: #071926; padding: 16px 40px; border-radius: 12px; margin: 0 0 32px;">
                                <span style="font-size: 36px; font-weight: 700; letter-spacing: 8px; color: #ffffff; font-family: 'Courier New', monospace;">%s</span>
                            </div>
                            <p style="color: #94a3b8; font-size: 13px; margin: 0; line-height: 1.5;">
                                If you didn't request this code, you can safely ignore this email.<br>
                                Do not share this code with anyone.
                            </p>
                        </div>
                        <div style="background: #f8fafc; padding: 20px 32px; text-align: center; border-top: 1px solid #e2e8f0;">
                            <p style="color: #94a3b8; font-size: 12px; margin: 0;">&copy; 2026 CertifyMe. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(otp);
    }

    /**
     * Custom checked-to-runtime exception for email delivery failures.
     * This allows the GlobalExceptionHandler to catch and return proper JSON responses
     * instead of generic 500s or false 404s.
     */
    public static class EmailDeliveryException extends RuntimeException {
        public EmailDeliveryException(String message) {
            super(message);
        }

        public EmailDeliveryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
package com.certifyme.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EmailService {

    @Value("${EMAIL_PASS:${spring.mail.password:}}")
    private String sendGridApiKey;

    /**
     * The verified sender address for outgoing emails.
     * In prod: set app.mail.from to your SendGrid verified sender email.
     * In dev: defaults to the local stub address.
     */
    @Value("${app.mail.from:verify.certifyme@gmail.com}")
    private String fromEmail;

    private final RestTemplate restTemplate;

    public EmailService() {
        this.restTemplate = new RestTemplate();
    }

    // @Async removed — running synchronously to fix silent async thread failure
    public void sendOtpEmail(String toEmail, String otp) {
        log.info("Sending OTP email to: {} (from: {}) via SendGrid API", toEmail, fromEmail);

        try {
            String content = """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>CertifyMe Verification</title>
                    </head>
                    <body style="margin: 0; padding: 0; background-color: #f4f4f4; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">

                    <table role="presentation" width="100%%" bgcolor="#f4f4f4" cellpadding="0" cellspacing="0" border="0" style="padding: 20px 0;">
                    <tr>
                    <td align="center">

                    <table role="presentation" width="100%%" bgcolor="#ffffff" cellpadding="0" cellspacing="0" border="0" style="max-width: 600px; width: 100%%; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05);">

                    <tr>
                    <td align="center" bgcolor="#071926" style="padding: 40px 20px; background-image: linear-gradient(to right, #071926, #0a253a);">
                    <span style="color: #ffffff; font-size: 32px; font-weight: bold;">
                    CertifyMe
                    </span>
                    </td>
                    </tr>

                    <tr>
                    <td align="center" style="padding: 50px 40px 40px 40px;">

                    <h1 style="margin: 0 0 25px 0; font-size: 26px; font-weight: 800; color: #111111;">
                    Admin Login Verification
                    </h1>

                    <p style="margin: 0 0 35px 0; font-size: 16px; color: #111111;">
                    Use the verification code below:
                    </p>

                    <table role="presentation">
                    <tr>
                    <td align="center" bgcolor="#000000" style="border-radius: 50px; padding: 18px 45px;">
                    <span style="font-size: 32px; font-weight: bold; color: #ffffff; letter-spacing: 8px; font-family: monospace;">%s</span>
                    </td>
                    </tr>
                    </table>

                    <p style="margin: 35px 0 0 0; font-size: 12px; color: #666666;">
                    This code is valid for 2 minutes.
                    </p>

                    </td>
                    </tr>

                    </table>

                    </td>
                    </tr>
                    </table>

                    </body>
                    </html>
                    """
                    .formatted(otp);

            Map<String, Object> requestBody = Map.of(
                    "personalizations", List.of(
                            Map.of(
                                    "to", List.of(Map.of("email", toEmail)),
                                    "subject", "Admin Login - Verification Code"
                            )
                    ),
                    "from", Map.of("email", fromEmail),
                    "content", List.of(
                            Map.of(
                                    "type", "text/html",
                                    "value", content
                            )
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(sendGridApiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity("https://api.sendgrid.com/v3/mail/send", entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("OTP email sent successfully to: {}", toEmail);
            } else {
                log.error("Failed to send OTP email to: {}. Response: {}", toEmail, response.getBody());
            }

        } catch (Exception e) {
            log.error("Exception occurred while sending OTP email to: {}", toEmail, e);
        }
    }
}
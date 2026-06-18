package com.certifyme.app.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;

/**
 * Enterprise-grade email service using the official SendGrid v3 API.
 * NO SMTP. NO JavaMailSender. NO Gmail. Pure SendGrid REST API only.
 *
 * <p>Email design follows the IBM Cloud email UI pattern:
 * pure-black header with teal aurora glow, clean white body,
 * prominent OTP block, IBM-style dark footer.
 * The CertifyMe logo is attached as an inline CID image via
 * SendGrid's Attachments API (no external image hosting needed).
 */
@Slf4j
@Service
public class EmailService {

    private static final String LOGO_HEADER_CID      = "certifyme-logo-header";
    private static final String LOGO_FOOTER_CID      = "certifyme-logo-footer";
    private static final String LOGO_HEADER_CLASSPATH = "static/images/CertifyMeFavicon1.png";
    private static final String LOGO_FOOTER_CLASSPATH = "static/images/CertifyMeFaviconFinal.png";
    private static final String SUPPORT_EMAIL         = "support.certifyme@gmail.com";
    private static final String YEAR                  = String.valueOf(java.time.Year.now().getValue());

    @Value("${app.sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${app.mail.from}")
    private String fromEmail;

    private SendGrid sendGrid;

    /** Base64-encoded header logo (CertifyMeFavicon1.png) loaded once at startup. */
    private String logoHeaderBase64;

    /** Base64-encoded footer logo (CertifyMeFaviconFinal.png) loaded once at startup. */
    private String logoFooterBase64;

    @PostConstruct
    public void init() {
        this.sendGrid = new SendGrid(sendGridApiKey);
        log.info("[EmailService] SendGrid API initialized. Verified sender: {}", fromEmail);

        // Pre-load and encode the header logo (CertifyMeFavicon1.png)
        try {
            ClassPathResource headerRes = new ClassPathResource(LOGO_HEADER_CLASSPATH);
            byte[] headerBytes = headerRes.getInputStream().readAllBytes();
            this.logoHeaderBase64 = Base64.getEncoder().encodeToString(headerBytes);
            log.info("[EmailService] Header logo loaded successfully ({} bytes).", headerBytes.length);
        } catch (IOException e) {
            log.warn("[EmailService] Could not load header logo from classpath '{}'. Cause: {}",
                    LOGO_HEADER_CLASSPATH, e.getMessage());
            this.logoHeaderBase64 = null;
        }

        // Pre-load and encode the footer logo (CertifyMeFaviconFinal.png)
        try {
            ClassPathResource footerRes = new ClassPathResource(LOGO_FOOTER_CLASSPATH);
            byte[] footerBytes = footerRes.getInputStream().readAllBytes();
            this.logoFooterBase64 = Base64.getEncoder().encodeToString(footerBytes);
            log.info("[EmailService] Footer logo loaded successfully ({} bytes).", footerBytes.length);
        } catch (IOException e) {
            log.warn("[EmailService] Could not load footer logo from classpath '{}'. Cause: {}",
                    LOGO_FOOTER_CLASSPATH, e.getMessage());
            this.logoFooterBase64 = null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Sends a personalised OTP email to the specified recipient.
     *
     * @param toEmail        recipient email address
     * @param recipientName  first name of the recipient (used in greeting)
     * @param otp            the one-time password to include in the email
     * @throws EmailDeliveryException if the email cannot be sent
     */
    public void sendOtpEmail(String toEmail, String recipientName, String otp) {
        log.info("[EmailService] Preparing OTP email for: {}", toEmail);

        Email from    = new Email(fromEmail, "CertifyMe Support");
        Email to      = new Email(toEmail);
        String subject = "Your CertifyMe Verification Code";
        Content content = new Content("text/html", buildOtpHtml(recipientName, otp));

        Mail mail = new Mail(from, subject, to, content);

        // Attach header logo as inline CID image
        if (logoHeaderBase64 != null) {
            Attachments headerLogoAttachment = new Attachments();
            headerLogoAttachment.setContent(logoHeaderBase64);
            headerLogoAttachment.setType("image/png");
            headerLogoAttachment.setFilename("certifyme-logo-header.png");
            headerLogoAttachment.setDisposition("inline");
            headerLogoAttachment.setContentId(LOGO_HEADER_CID);
            mail.addAttachments(headerLogoAttachment);
        }

        // Attach footer logo as inline CID image
        if (logoFooterBase64 != null) {
            Attachments footerLogoAttachment = new Attachments();
            footerLogoAttachment.setContent(logoFooterBase64);
            footerLogoAttachment.setType("image/png");
            footerLogoAttachment.setFilename("certifyme-logo-footer.png");
            footerLogoAttachment.setDisposition("inline");
            footerLogoAttachment.setContentId(LOGO_FOOTER_CID);
            mail.addAttachments(footerLogoAttachment);
        }

        dispatch(mail, toEmail);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /** Dispatches a built {@link Mail} object via the SendGrid REST API. */
    private void dispatch(Mail mail, String toEmail) {
        try {
            log.info("[EmailService] Building mail payload for: {}", toEmail);
            String mailBody = mail.build();
            log.debug("[EmailService] Mail payload built successfully ({} chars)", mailBody.length());

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mailBody);

            log.info("[EmailService] Sending request to SendGrid API...");
            Response response = sendGrid.api(request);
            int statusCode   = response.getStatusCode();
            log.info("[EmailService] SendGrid response → status={}, to={}", statusCode, toEmail);

            if (statusCode >= 200 && statusCode < 300) {
                log.info("[EmailService] ✅ Email delivered successfully to: {}", toEmail);
            } else {
                log.error("[EmailService] ❌ SendGrid rejected email → status={}, body={}", statusCode, response.getBody());
                throw new EmailDeliveryException(
                        "SendGrid returned status " + statusCode + ": " + response.getBody());
            }

        } catch (EmailDeliveryException e) {
            throw e;
        } catch (Exception e) {
            log.error("[EmailService] ❌ Exception during email dispatch to {}: {} — {}",
                    toEmail, e.getClass().getSimpleName(), e.getMessage(), e);
            throw new EmailDeliveryException("Failed to send email. Please try again later.", e);
        }
    }

    /**
     * Builds the IBM Cloud-inspired HTML email for OTP delivery.
     *
     * <p>Design tokens:
     * <ul>
     *   <li>Header  — pure black (#000000) with radial teal/blue aurora glow</li>
     *   <li>Divider — 3 px IBM blue-to-cyan gradient line</li>
     *   <li>Body    — white (#ffffff), IBM Carbon text colours (#161616 / #525252)</li>
     *   <li>OTP box — dark (#161616) rounded block, monospace digits</li>
     *   <li>Footer  — dark (#161616), muted grey copyright + "CM" badge</li>
     * </ul>
     *
     * @param name first name of the recipient
     * @param otp  the 6-digit (or any length) one-time password
     */
    private String buildOtpHtml(String name, String otp) {

        // Greeting name — fall back gracefully if null/blank
        String displayName = (name != null && !name.isBlank()) ? name : "there";

        // OTP displayed with wide letter-spacing for readability
        String spacedOtp = String.join(" ", otp.split("")); // plain space between digits for max compatibility

        // Header logo img tag
        String headerLogoImg = (logoHeaderBase64 != null)
                ? "<img src=\"cid:" + LOGO_HEADER_CID + "\" alt=\"CertifyMe\" width=\"44\" height=\"44\" "
                + "style=\"display:block;border-radius:8px;\" />"
                : "";

        // Footer logo img tag
        String footerLogoImg = (logoFooterBase64 != null)
                ? "<img src=\"cid:" + LOGO_FOOTER_CID + "\" alt=\"CertifyMe\" width=\"28\" height=\"28\" "
                + "style=\"display:block;border-radius:6px;\" />"
                : "";

        return """
<!DOCTYPE html>
<html lang="en" xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <meta http-equiv="X-UA-Compatible" content="IE=edge" />
  <title>CertifyMe Verification Code</title>
</head>
<body style="margin:0;padding:0;background-color:#f2f2f2;font-family:Arial,Helvetica,sans-serif;-webkit-text-size-adjust:100%%;-ms-text-size-adjust:100%%;">

  <!--[if mso]><table width="100%%" cellpadding="0" cellspacing="0" border="0"><tr><td align="center"><![endif]-->

  <!-- ░░░ OUTER WRAPPER ░░░ -->
  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0"
         style="background-color:#f2f2f2;">
    <tr>
      <td align="center" style="padding:32px 12px 48px;">

        <!-- ┌─────────────────────────────────────────────────┐ -->
        <!-- │                   EMAIL CARD                    │ -->
        <!-- └─────────────────────────────────────────────────┘ -->
        <table role="presentation" width="600" cellpadding="0" cellspacing="0" border="0"
               style="max-width:600px;width:100%%;background:#ffffff;">

          <!-- ══════════════════════ HEADER ══════════════════════ -->
          <tr>
            <td style="
              background-color:#000000;
              background-image:radial-gradient(ellipse at 82%% 48%%,
                rgba(0,188,212,0.38) 0%%,
                rgba(0,90,175,0.22) 42%%,
                transparent 72%%);
              padding:40px 28px 38px;
            ">
              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                <tr>
                  <!-- Brand name + subtitle -->
                  <td style="vertical-align:middle;">
                    <span style="
                      font-family:Arial,Helvetica,sans-serif;
                      font-size:21px;
                      font-weight:700;
                      color:#ffffff;
                      letter-spacing:0.4px;
                      line-height:1;
                    ">CertifyMe</span>
                    <p style="
                      margin:5px 0 0;
                      font-size:11px;
                      color:rgba(255,255,255,0.55);
                      letter-spacing:0.6px;
                      text-transform:uppercase;
                    ">Certification Tracking Platform</p>
                  </td>
                  <!-- Logo (CID inline) -->
                  <td align="right" style="vertical-align:middle;">
                    %s
                  </td>
                </tr>
              </table>
            </td>
          </tr>

          <!-- ══════════ IBM BLUE GRADIENT DIVIDER ══════════ -->
          <tr>
            <td style="
              height:3px;
              background:linear-gradient(to right,#0062ff 0%%,#00b4c8 55%%,#e0e0e0 100%%);
              font-size:0;
              line-height:0;
            ">&nbsp;</td>
          </tr>

          <!-- ══════════════════════ BODY ══════════════════════ -->
          <tr>
            <td style="padding:36px 32px 8px;background:#ffffff;">

              <!-- Greeting -->
              <p style="margin:0 0 22px;font-size:15px;color:#161616;line-height:1.6;">
                Hi <strong>%s</strong> sir,
              </p>

              <!-- Intro text -->
              <p style="margin:0 0 6px;font-size:15px;color:#161616;line-height:1.6;font-weight:600;">
                Verification Code
              </p>
              <p style="margin:0 0 28px;font-size:14px;color:#525252;line-height:1.7;">
                Use the code below to verify your identity.
                This code is valid for <strong style="color:#161616;">10 minutes</strong>.
              </p>

              <!-- ── OTP CODE ── -->
              <p style="margin:0 0 28px;font-size:28px;font-family:Arial,Helvetica,sans-serif;font-weight:700;color:#161616;letter-spacing:8px;line-height:1.4;">%s</p>

              <!-- Security notice -->
              <p style="margin:0 0 6px;font-size:13px;color:#525252;line-height:1.7;">
                If you didn't request this code, you can safely ignore this email.
              </p>
              <p style="margin:0;font-size:13px;color:#525252;line-height:1.7;">
                Do not share this code with anyone.
              </p>

            </td>
          </tr>

          <!-- ══════════════════════ SUPPORT NOTICE ══════════════════════ -->
          <tr>
            <td style="padding:28px 32px 8px;background:#ffffff;">
              <p style="margin:0;font-size:13px;color:#525252;line-height:1.8;">
                If you have questions or believe that there is a mistake or error,
                please contact CertifyMe Support at
                <a href="mailto:%s"
                   style="color:#0062ff;text-decoration:none;">%s</a>.
              </p>
            </td>
          </tr>

          <!-- ══════════════════════ SIGN-OFF ══════════════════════ -->
          <tr>
            <td style="padding:24px 32px 32px;background:#ffffff;">
              <p style="margin:0;font-size:14px;color:#161616;line-height:1.9;">
                Thank you,<br />
                <strong>CertifyMe Support</strong>
              </p>
            </td>
          </tr>

          <!-- ══════════════════════ FOOTER ══════════════════════ -->
          <tr>
            <td style="background-color:#161616;padding:16px 32px;">
              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                <tr>
                  <td style="vertical-align:middle;">
                    <p style="margin:0;font-size:11px;color:#8d8d8d;line-height:1.6;">
                      &copy;&nbsp;Copyright CertifyMe %s.&nbsp;All rights reserved.
                    </p>
                  </td>
                  <td align="right" style="vertical-align:middle;">
                    %s
                  </td>
                </tr>
              </table>
            </td>
          </tr>

        </table>
        <!-- /EMAIL CARD -->

      </td>
    </tr>
  </table>

  <!--[if mso]></td></tr></table><![endif]-->

</body>
</html>
""".formatted(headerLogoImg, displayName, spacedOtp, SUPPORT_EMAIL, SUPPORT_EMAIL, YEAR, footerLogoImg);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EXCEPTION
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Custom runtime exception for email delivery failures.
     * Allows the GlobalExceptionHandler to return proper JSON instead of generic 500s.
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
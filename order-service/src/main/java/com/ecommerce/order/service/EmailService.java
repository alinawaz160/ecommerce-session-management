package com.ecommerce.order.service;

import com.ecommerce.order.event.OrderEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Async
    public void sendOrderConfirmation(OrderEvent event) {
        if (event.getUserEmail() == null || event.getUserEmail().isBlank()) {
            log.warn("No email address for order {} — skipping confirmation email", event.getOrderNumber());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress, fromName);
            helper.setTo(event.getUserEmail());
            helper.setSubject("Order Confirmed — " + event.getOrderNumber());
            helper.setText(buildHtml(event), true);

            mailSender.send(message);
            log.info("Confirmation email sent to {} for order {}", event.getUserEmail(), event.getOrderNumber());

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send confirmation email for order {}: {}", event.getOrderNumber(), e.getMessage());
        }
    }

    private String buildHtml(OrderEvent event) {
        String date = event.getCreatedAt() != null
            ? event.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
            : "N/A";

        String total = event.getTotalAmount() != null
            ? "$" + event.getTotalAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
            : "N/A";

        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <style>
                body { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; padding: 0; }
                .container { max-width: 600px; margin: 40px auto; background: #ffffff; border-radius: 8px;
                             overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
                .header { background: #2c3e50; color: #ffffff; padding: 24px 32px; }
                .header h1 { margin: 0; font-size: 24px; }
                .header p  { margin: 4px 0 0; font-size: 14px; color: #bdc3c7; }
                .body { padding: 32px; }
                .body p { color: #555; line-height: 1.6; }
                .order-box { background: #f8f9fa; border-left: 4px solid #2ecc71;
                             border-radius: 4px; padding: 16px 20px; margin: 20px 0; }
                .order-box .label { font-size: 12px; color: #888; text-transform: uppercase; letter-spacing: 1px; }
                .order-box .value { font-size: 20px; font-weight: bold; color: #2c3e50; margin-top: 4px; }
                .details-table { width: 100%%; border-collapse: collapse; margin-top: 16px; }
                .details-table td { padding: 8px 0; border-bottom: 1px solid #eee; color: #555; font-size: 14px; }
                .details-table td:last-child { text-align: right; font-weight: bold; color: #2c3e50; }
                .footer { background: #f8f9fa; padding: 20px 32px; text-align: center;
                          font-size: 12px; color: #aaa; border-top: 1px solid #eee; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1>ShopEasy</h1>
                  <p>Order Confirmation</p>
                </div>
                <div class="body">
                  <p>Hi there,</p>
                  <p>Thank you for your order! We've received it and it's being processed.</p>
                  <div class="order-box">
                    <div class="label">Order Number</div>
                    <div class="value">%s</div>
                  </div>
                  <table class="details-table">
                    <tr>
                      <td>Date</td>
                      <td>%s</td>
                    </tr>
                    <tr>
                      <td>Total Amount</td>
                      <td>%s</td>
                    </tr>
                    <tr>
                      <td>Status</td>
                      <td>%s</td>
                    </tr>
                  </table>
                  <p style="margin-top: 24px;">You'll receive another update when your order ships.</p>
                  <p>Thanks for shopping with us!</p>
                </div>
                <div class="footer">
                  &copy; 2026 ShopEasy. All rights reserved.
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                event.getOrderNumber(),
                date,
                total,
                event.getStatus()
            );
    }
}

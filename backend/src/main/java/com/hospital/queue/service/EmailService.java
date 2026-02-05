package com.hospital.queue.service;

import com.hospital.queue.entity.Token;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${app.email.from-name:Smart Queue Management}")
    private String fromName;

    @Value("${app.email.from-address:noreply@smartqueue.com}")
    private String fromAddress;

    @Async
    public void sendTokenConfirmationEmail(Token token) {
        if (!emailEnabled || token.getPatient().getEmail() == null || token.getPatient().getEmail().isEmpty()) {
            log.info("Email notification skipped - email not enabled or patient has no email");
            return;
        }

        try {
            Context context = new Context();
            context.setVariable("patientName", token.getPatient().getFirstName() + " " + token.getPatient().getLastName());
            context.setVariable("tokenNumber", token.getTokenNumber());
            context.setVariable("department", token.getDepartment().getName());
            context.setVariable("domainName", token.getDepartment().getDomain() != null ? 
                    token.getDepartment().getDomain().getName() : "Service");
            context.setVariable("doctorName", token.getDoctor() != null ? 
                    "Dr. " + token.getDoctor().getFirstName() + " " + token.getDoctor().getLastName() : "Not assigned");
            context.setVariable("tokenDate", token.getTokenDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
            context.setVariable("generatedAt", token.getGeneratedAt().format(DateTimeFormatter.ofPattern("hh:mm a")));
            context.setVariable("priority", token.getPriority().name().replace("_", " "));
            context.setVariable("roomNumber", token.getDoctor() != null ? token.getDoctor().getRoomNumber() : "Will be assigned");

            String htmlContent = templateEngine.process("token-confirmation", context);

            sendHtmlEmail(
                    token.getPatient().getEmail(),
                    "Token Confirmation - " + token.getTokenNumber(),
                    htmlContent
            );

            log.info("Token confirmation email sent to: {}", token.getPatient().getEmail());

        } catch (Exception e) {
            log.error("Failed to send token confirmation email to: {}", token.getPatient().getEmail(), e);
        }
    }

    @Async
    public void sendQueueUpdateEmail(Token token, int position, int estimatedWaitMinutes) {
        sendQueueUpdateEmail(token, position, estimatedWaitMinutes, null, null);
    }

    @Async
    public void sendQueueUpdateEmail(Token token, int position, int estimatedWaitMinutes, 
                                      Integer previousPosition, String positionChangeReason) {
        if (!emailEnabled || token.getPatient().getEmail() == null || token.getPatient().getEmail().isEmpty()) {
            log.info("Queue update email skipped - email not enabled or patient has no email");
            return;
        }

        try {
            Context context = new Context();
            context.setVariable("patientName", token.getPatient().getFirstName() + " " + token.getPatient().getLastName());
            context.setVariable("tokenNumber", token.getTokenNumber());
            context.setVariable("department", token.getDepartment().getName());
            context.setVariable("position", position);
            context.setVariable("estimatedWaitMinutes", estimatedWaitMinutes);
            context.setVariable("doctorName", token.getDoctor() != null ? 
                    "Dr. " + token.getDoctor().getFirstName() + " " + token.getDoctor().getLastName() : "Service Counter");
            context.setVariable("roomNumber", token.getDoctor() != null ? token.getDoctor().getRoomNumber() : "Counter");
            context.setVariable("isAlmostTurn", position <= 3);
            
            // Position change tracking for transparency
            boolean positionChanged = previousPosition != null && !previousPosition.equals(position) && previousPosition < position;
            context.setVariable("positionChanged", positionChanged);
            context.setVariable("previousPosition", previousPosition);
            context.setVariable("positionChangeReason", positionChangeReason != null ? positionChangeReason : 
                    "A priority case has been added to the queue ahead of you.");

            String htmlContent = templateEngine.process("queue-update", context);

            String subject;
            if (positionChanged) {
                subject = "📢 Queue Position Update - " + token.getTokenNumber();
            } else if (position <= 3) {
                subject = "⚠️ Your Turn is Approaching! - " + token.getTokenNumber();
            } else {
                subject = "📊 Queue Update - " + token.getTokenNumber();
            }

            sendHtmlEmail(
                    token.getPatient().getEmail(),
                    subject,
                    htmlContent
            );

            log.info("Queue update email sent to: {} (Position: {}, Changed: {})", 
                    token.getPatient().getEmail(), position, positionChanged);

        } catch (Exception e) {
            log.error("Failed to send queue update email to: {}", token.getPatient().getEmail(), e);
        }
    }

    @Async
    public void sendTurnNotificationEmail(Token token) {
        if (!emailEnabled || token.getPatient().getEmail() == null || token.getPatient().getEmail().isEmpty()) {
            log.info("Turn notification email skipped - email not enabled or patient has no email");
            return;
        }

        try {
            Context context = new Context();
            context.setVariable("patientName", token.getPatient().getFirstName() + " " + token.getPatient().getLastName());
            context.setVariable("tokenNumber", token.getTokenNumber());
            context.setVariable("department", token.getDepartment().getName());
            context.setVariable("doctorName", token.getDoctor() != null ? 
                    "Dr. " + token.getDoctor().getFirstName() + " " + token.getDoctor().getLastName() : "Service Counter");
            context.setVariable("roomNumber", token.getDoctor() != null ? token.getDoctor().getRoomNumber() : "Counter");

            String htmlContent = templateEngine.process("turn-notification", context);

            sendHtmlEmail(
                    token.getPatient().getEmail(),
                    "🔔 IT'S YOUR TURN! - " + token.getTokenNumber(),
                    htmlContent
            );

            log.info("Turn notification email sent to: {}", token.getPatient().getEmail());

        } catch (Exception e) {
            log.error("Failed to send turn notification email to: {}", token.getPatient().getEmail(), e);
        }
    }

    @Async
    public void sendConsultationCompletedEmail(Token token) {
        if (!emailEnabled || token.getPatient().getEmail() == null || token.getPatient().getEmail().isEmpty()) {
            log.info("Consultation completed email skipped - email not enabled or patient has no email");
            return;
        }

        try {
            Context context = new Context();
            context.setVariable("patientName", token.getPatient().getFirstName() + " " + token.getPatient().getLastName());
            context.setVariable("tokenNumber", token.getTokenNumber());
            context.setVariable("department", token.getDepartment().getName());
            context.setVariable("doctorName", token.getDoctor() != null ? 
                    "Dr. " + token.getDoctor().getFirstName() + " " + token.getDoctor().getLastName() : "Service Counter");
            context.setVariable("consultationDate", token.getTokenDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
            context.setVariable("domainName", token.getDepartment().getDomain() != null ? 
                    token.getDepartment().getDomain().getName() : "Service");

            String htmlContent = templateEngine.process("consultation-completed", context);

            sendHtmlEmail(
                    token.getPatient().getEmail(),
                    "Consultation Completed - " + token.getTokenNumber(),
                    htmlContent
            );

            log.info("Consultation completed email sent to: {}", token.getPatient().getEmail());

        } catch (Exception e) {
            log.error("Failed to send consultation completed email to: {}", token.getPatient().getEmail(), e);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException, java.io.UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromAddress, fromName);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}

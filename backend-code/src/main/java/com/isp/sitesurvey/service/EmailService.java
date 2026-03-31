package com.isp.sitesurvey.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Email Service - Handles email sending operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Send OTP email asynchronously
     */
    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset OTP - Site Survey Tool");
            message.setText(buildOtpEmailBody(otp));

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    /**
     * Build OTP email body
     */
    private String buildOtpEmailBody(String otp) {
        return String.format("""
            Dear User,
            
            You have requested to reset your password for Site Survey Tool.
            
            Your OTP (One-Time Password) is: %s
            
            This OTP is valid for 30 seconds only.
            
            If you did not request this, please ignore this email.
            
            Best regards,
            Site Survey Tool Team
            """, otp);
    }

    /**
     * Send welcome email after successful registration
     */
    @Async
    public void sendWelcomeEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to Site Survey Tool");
            message.setText(buildWelcomeEmailBody(username));

            mailSender.send(message);
            log.info("Welcome email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }

    /**
     * Build welcome email body
     */
    private String buildWelcomeEmailBody(String username) {
        return String.format("""
            Dear %s,
            
            Welcome to Site Survey Tool!
            
            Your account has been successfully created.
            You can now log in and start using our platform to plan and execute
            network equipment installations.
            
            If you have any questions, please contact our support team.
            
            Best regards,
            Site Survey Tool Team
            """, username);
    }
}
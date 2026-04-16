package com.findash.userservice.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendVerificationCode(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("FinDash - Your verification code");
        message.setText(
                        "Hi,\n\n" +
                        "Your FinDash verification code is:\n\n" +
                        " " + otp + "\n\n" +
                        "This code expires in 10 minutes.\n\n" +
                        "If you didn't request this, please ignore this email.\n\n" +
                        "- The FinDash Team"

        );
        mailSender.send(message);
    }

    @Async
    public void sendPasswordResetCode(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("FinDash - Password reset code");
        message.setText(
                        "Hi,\n\n" +
                        "We received a request to reset your FinDash password.\n\n" +
                        "Your reset code is:\n\n" +
                        " " + otp + "\n\n" +
                        "This code expires in 10 minutes.\n\n" +
                        "If you didn't request this, please ignore this email.\n\n" +
                        "- The FinDash Team"
        );
        mailSender.send(message);
    }
}

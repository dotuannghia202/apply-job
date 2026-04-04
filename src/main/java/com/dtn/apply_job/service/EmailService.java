package com.dtn.apply_job.service;


import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordEmail(String toEmail, String generatedPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Chào mừng đến với Nền tảng Tìm việc - JobPortal");
        message.setText("Xin chào,\n\n" +
                "Tài khoản của bạn đã được tạo thành công.\n" +
                "Dưới đây là mật khẩu đăng nhập tạm thời của bạn: " + generatedPassword + "\n\n" +
                "Vui lòng đăng nhập và đổi mật khẩu sớm nhất có thể để bảo mật tài khoản.\n\n" +
                "Trân trọng,\nBan quản trị hệ thống.");

        mailSender.send(message);
    }
}

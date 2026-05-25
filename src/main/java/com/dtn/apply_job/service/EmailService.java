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

    // 2. HÀM DÙNG KHI NGƯỜI DÙNG QUÊN MẬT KHẨU (HÀM MỚI THÊM)
    public void sendResetPasswordEmail(String toEmail, String newPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("JobPortal - Yêu cầu Cấp lại Mật khẩu");

        // Nội dung Email chuyên nghiệp đúng ngữ cảnh
        message.setText(
                "Xin chào,\n\n" +
                        "Chúng tôi nhận được yêu cầu cấp lại mật khẩu cho tài khoản liên kết với địa chỉ email này.\n\n" +
                        "Đây là mật khẩu mới của bạn: " + newPassword + "\n\n" +
                        "Vui lòng sử dụng mật khẩu này để đăng nhập vào hệ thống. " +
                        "Sau khi đăng nhập thành công, hãy vào mục 'Hồ sơ cá nhân' để ĐỔI MẬT KHẨU nhằm đảm bảo an toàn cho tài khoản của bạn.\n\n" +
                        "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này hoặc liên hệ với bộ phận Hỗ trợ.\n\n" +
                        "Trân trọng,\nBan quản trị JobPortal."
        );
        mailSender.send(message);
    }

    public void sendAccountLockedEmail(String toEmail, String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("CẢNH BÁO: Tài khoản của bạn đã bị khóa - JobPortal");

        message.setText(
                "Xin chào " + name + ",\n\n" +
                        "Chúng tôi xin thông báo rằng tài khoản của bạn trên nền tảng JobPortal đã bị tạm khóa bởi Quản trị viên.\n\n" +
                        "Lý do: Vi phạm chính sách cộng đồng hoặc phát hiện hoạt động bất thường.\n" +
                        "Hiện tại, bạn sẽ không thể đăng nhập và sử dụng các dịch vụ của chúng tôi.\n\n" +
                        "Nếu bạn cho rằng đây là một sự nhầm lẫn, vui lòng liên hệ trực tiếp với bộ phận Hỗ trợ (Support) để được giải quyết.\n\n" +
                        "Trân trọng,\nBan quản trị JobPortal."
        );
        mailSender.send(message);
    }
}

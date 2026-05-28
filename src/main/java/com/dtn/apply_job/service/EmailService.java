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

    // HÀM 3: GỬI EMAIL KHI CÔNG TY ĐƯỢC ADMIN DUYỆT THÀNH CÔNG
    // ====================================================================
    public void sendCompanyApprovedEmail(String toEmail, String companyName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("JobPortal - Hồ sơ doanh nghiệp đã được phê duyệt!");

        message.setText(
                "Xin chào,\n\n" +
                        "Chúc mừng! Hồ sơ đăng ký doanh nghiệp của công ty '" + companyName + "' đã được Quản trị viên của chúng tôi phê duyệt thành công.\n\n" +
                        "Ngay bây giờ, bạn có thể truy cập vào Hệ thống quản trị (Employer Dashboard) để bắt đầu sử dụng các tính năng AI, đăng tải tin tuyển dụng và tìm kiếm những ứng viên tài năng nhất.\n\n" +
                        "Nếu cần hỗ trợ thêm, vui lòng liên hệ với chúng tôi.\n\n" +
                        "Trân trọng,\n" +
                        "Ban quản trị JobPortal."
        );
        mailSender.send(message);
    }

    // ====================================================================
    // HÀM 4: GỬI EMAIL KHI CÔNG TY BỊ ADMIN TỪ CHỐI (TÙY CHỌN)
    // ====================================================================
    public void sendCompanyRejectedEmail(String toEmail, String companyName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("JobPortal - Thông báo về hồ sơ doanh nghiệp");

        message.setText(
                "Xin chào,\n\n" +
                        "Cảm ơn bạn đã đăng ký hồ sơ doanh nghiệp cho công ty '" + companyName + "' trên nền tảng của chúng tôi.\n\n" +
                        "Tuy nhiên, sau khi xem xét, chúng tôi rất tiếc phải thông báo rằng hồ sơ công ty của bạn hiện chưa đáp ứng đủ các tiêu chuẩn hoặc thông tin chưa rõ ràng để được phê duyệt.\n\n" +
                        "Vui lòng đăng nhập lại hệ thống, kiểm tra và cập nhật lại thông tin công ty (Tên, Địa chỉ, Mã số thuế, Logo...) sao cho chính xác nhất. Sau khi cập nhật, hệ thống sẽ tự động gửi lại yêu cầu phê duyệt cho Quản trị viên.\n\n" +
                        "Trân trọng,\n" +
                        "Ban quản trị JobPortal."
        );
        mailSender.send(message);
    }
}

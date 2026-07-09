package com.dtn.apply_job.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    private String buildHtmlTemplate(String title, String mainContent) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f3f4f6; margin: 0; padding: 0; }
                        .email-container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
                        .header { background-color: #16a34a; padding: 25px 20px; text-align: center; }
                        .header h1 { margin: 0; color: #ffffff; font-size: 28px; letter-spacing: 1px; }
                        .content { padding: 30px 40px; color: #374151; line-height: 1.6; font-size: 16px; }
                        .content h2 { color: #111827; font-size: 20px; margin-top: 0; border-bottom: 2px solid #e5e7eb; padding-bottom: 10px; }
                        .highlight-box { background-color: #f8fafc; border: 1px dashed #cbd5e1; padding: 15px; text-align: center; border-radius: 8px; margin: 20px 0; font-size: 22px; font-weight: bold; color: #16a34a; letter-spacing: 2px;}
                        .footer { background-color: #f8fafc; padding: 20px; text-align: center; color: #64748b; font-size: 13px; border-top: 1px solid #e5e7eb; }
                        .btn { display: inline-block; padding: 12px 25px; background-color: #16a34a; color: white; text-decoration: none; border-radius: 6px; font-weight: bold; margin-top: 20px; }
                    </style>
                </head>
                <body>
                    <div class="email-container">
                        <div class="header">
                            <h1>Job Portal</h1>
                        </div>
                        <div class="content">
                            <h2>%s</h2>
                            %s
                        </div>
                        <div class="footer">
                            <p>© 2026 Job Portal Platform. Tất cả các quyền được bảo lưu.</p>
                            <p>Đây là email tự động, vui lòng không trả lời email này.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(title, mainContent);
    }


    private void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            System.out.println("Đã gửi HTML Email thành công tới: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("Lỗi khi gửi email: " + e.getMessage());
        }
    }


    public void sendPasswordEmail(String toEmail, String generatedPassword) {
        String title = "Chào mừng bạn đến với Job Portal \uD83C\uDF89";
        String content = """
                <p>Xin chào,</p>
                <p>Tài khoản của bạn đã được tạo thành công trên hệ thống của chúng tôi.</p>
                <p>Dưới đây là mật khẩu đăng nhập tạm thời của bạn:</p>
                <div class="highlight-box">%s</div>
                <p>Vui lòng đăng nhập và đổi mật khẩu sớm nhất có thể để đảm bảo bảo mật tài khoản.</p>
                """.formatted(generatedPassword);

        String htmlBody = buildHtmlTemplate(title, content);
        sendHtmlEmail(toEmail, "Chào mừng đến với Nền tảng Tìm việc - JobPortal", htmlBody);
    }


    public void sendResetPasswordEmail(String toEmail, String newPassword) {
        String title = "Yêu cầu cấp lại mật khẩu \uD83D\uDD12";
        String content = """
                <p>Xin chào,</p>
                <p>Chúng tôi nhận được yêu cầu cấp lại mật khẩu cho tài khoản liên kết với địa chỉ email này.</p>
                <p>Đây là mật khẩu mới của bạn:</p>
                <div class="highlight-box">%s</div>
                <p>Vui lòng sử dụng mật khẩu này để đăng nhập vào hệ thống. Sau khi đăng nhập thành công, hãy vào mục <b>Hồ sơ cá nhân</b> để <b>ĐỔI MẬT KHẨU</b> nhằm đảm bảo an toàn.</p>
                <p><i>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này hoặc liên hệ với bộ phận Hỗ trợ ngay lập tức.</i></p>
                """.formatted(newPassword);

        String htmlBody = buildHtmlTemplate(title, content);
        sendHtmlEmail(toEmail, "JobPortal - Yêu cầu Cấp lại Mật khẩu", htmlBody);
    }


    public void sendAccountLockedEmail(String toEmail, String name) {
        String title = "CẢNH BÁO BẢO MẬT ⚠️";
        String content = """
                <p>Xin chào <b>%s</b>,</p>
                <p style="color: #dc2626; font-weight: bold;">Chúng tôi xin thông báo rằng tài khoản của bạn trên nền tảng JobPortal đã bị tạm khóa bởi Quản trị viên.</p>
                <p><b>Lý do:</b> Vi phạm chính sách cộng đồng hoặc phát hiện hoạt động bất thường.</p>
                <p>Hiện tại, bạn sẽ không thể đăng nhập và sử dụng các dịch vụ của chúng tôi.</p>
                <p>Nếu bạn cho rằng đây là một sự nhầm lẫn, vui lòng liên hệ trực tiếp với bộ phận Hỗ trợ (Support) để được giải quyết.</p>
                """.formatted(name);

        String htmlBody = buildHtmlTemplate(title, content);
        sendHtmlEmail(toEmail, "CẢNH BÁO: Tài khoản của bạn đã bị khóa - JobPortal", htmlBody);
    }


    public void sendCompanyApprovedEmail(String toEmail, String companyName) {
        String title = "Hồ sơ doanh nghiệp đã được phê duyệt! ✅";
        String content = """
                <p>Xin chào,</p>
                <p>Chúc mừng! Hồ sơ đăng ký doanh nghiệp của công ty <b>'%s'</b> đã được Quản trị viên của chúng tôi phê duyệt thành công.</p>
                <p>Ngay bây giờ, bạn có thể truy cập vào Hệ thống quản trị (Employer Dashboard) để bắt đầu sử dụng các tính năng AI, đăng tải tin tuyển dụng và tìm kiếm những ứng viên tài năng nhất.</p>
                <center>
                    <a href="https:
                </center>
                <br>
                <p>Nếu cần hỗ trợ thêm, vui lòng liên hệ với chúng tôi.</p>
                """.formatted(companyName);

        String htmlBody = buildHtmlTemplate(title, content);
        sendHtmlEmail(toEmail, "JobPortal - Hồ sơ doanh nghiệp đã được phê duyệt!", htmlBody);
    }

    // =======================================================================
    // 5. EMAIL CÔNG TY BỊ TỪ CHỐI
    // =======================================================================
    public void sendCompanyRejectedEmail(String toEmail, String companyName) {
        String title = "Thông báo về hồ sơ doanh nghiệp ❌";
        String content = """
                <p>Xin chào,</p>
                <p>Cảm ơn bạn đã đăng ký hồ sơ doanh nghiệp cho công ty <b>'%s'</b> trên nền tảng của chúng tôi.</p>
                <p>Tuy nhiên, sau khi xem xét, chúng tôi rất tiếc phải thông báo rằng hồ sơ công ty của bạn hiện chưa đáp ứng đủ các tiêu chuẩn hoặc thông tin chưa rõ ràng để được phê duyệt.</p>
                <p>Vui lòng đăng nhập lại hệ thống, kiểm tra và cập nhật lại thông tin công ty (Tên, Địa chỉ, Mã số thuế, Logo...) sao cho chính xác nhất.</p>
                <p><i>Sau khi cập nhật, hệ thống sẽ tự động gửi lại yêu cầu phê duyệt cho Quản trị viên.</i></p>
                """.formatted(companyName);

        String htmlBody = buildHtmlTemplate(title, content);
        sendHtmlEmail(toEmail, "JobPortal - Thông báo về hồ sơ doanh nghiệp", htmlBody);
    }

}
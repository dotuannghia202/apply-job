package com.dtn.apply_job.service;

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

    private String buildHtmlTemplate(String title, String mainContent, boolean isEnglish) {
        String footerCopyright = isEnglish
                ? "© 2026 Job Portal Platform. All rights reserved."
                : "© 2026 Job Portal Platform. Tất cả các quyền được bảo lưu.";
        String footerAutoMsg = isEnglish
                ? "This is an automated email, please do not reply to this email."
                : "Đây là email tự động, vui lòng không trả lời email này.";

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f3f4f6; margin: 0; padding: 20px 0; }
                        .email-container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; border: 1px solid #cbd5e1; border-bottom: 4px solid #16a34a; box-shadow: 0 4px 15px rgba(0,0,0,0.06); }
                        .header { background-color: #16a34a; padding: 25px 20px; text-align: center; }
                        .header h1 { margin: 0; color: #ffffff; font-size: 28px; letter-spacing: 1px; }
                        .content { padding: 30px 40px; color: #374151; line-height: 1.6; font-size: 16px; }
                        .content h2 { color: #111827; font-size: 20px; margin-top: 0; border-bottom: 2px solid #e5e7eb; padding-bottom: 12px; margin-bottom: 20px; }
                        .highlight-box { background-color: #f8fafc; border: 1px dashed #cbd5e1; padding: 15px; text-align: center; border-radius: 8px; margin: 20px 0; font-size: 22px; font-weight: bold; color: #16a34a; letter-spacing: 2px;}
                        .footer { background-color: #f8fafc; padding: 20px; text-align: center; color: #64748b; font-size: 13px; border-top: 1px solid #cbd5e1; }
                        .footer p { margin: 4px 0; }
                        .btn { display: inline-block; padding: 12px 25px; background-color: #16a34a; color: #ffffff !important; text-decoration: none; border-radius: 6px; font-weight: bold; margin-top: 15px; }
                    </style>
                </head>
                <body style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f3f4f6; margin: 0; padding: 20px 0;">
                    <div class="email-container" style="max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; border: 1px solid #cbd5e1; border-bottom: 4px solid #16a34a; box-shadow: 0 4px 15px rgba(0,0,0,0.06);">
                        <div class="header" style="background-color: #16a34a; padding: 25px 20px; text-align: center;">
                            <h1 style="margin: 0; color: #ffffff; font-size: 28px; letter-spacing: 1px;">Job Portal</h1>
                        </div>
                        <div class="content" style="padding: 30px 40px; color: #374151; line-height: 1.6; font-size: 16px;">
                            <h2 style="color: #111827; font-size: 20px; margin-top: 0; border-bottom: 2px solid #e5e7eb; padding-bottom: 12px; margin-bottom: 20px;">%s</h2>
                            %s
                        </div>
                        <div class="footer" style="background-color: #f8fafc; padding: 20px; text-align: center; color: #64748b; font-size: 13px; border-top: 1px solid #cbd5e1;">
                            <p style="margin: 4px 0; color: #64748b;">%s</p>
                            <p style="margin: 4px 0; color: #94a3b8; font-size: 12px;">%s</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(title, mainContent, footerCopyright, footerAutoMsg);
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
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email tới " + toEmail + ": " + e.getMessage());
        }
    }

    // =======================================================================
    // 1. EMAIL MẬT KHẨU TẠM THỜI (Tạo tài khoản)
    // =======================================================================
    public void sendPasswordEmail(String toEmail, String generatedPassword, String lang) {
        boolean isEnglish = "en".equalsIgnoreCase(lang);
        String subject = isEnglish
                ? "Welcome to Job Portal \uD83C\uDF89"
                : "Chào mừng đến với Nền tảng Tìm việc - JobPortal";
        String title = isEnglish
                ? "Welcome to Job Portal \uD83C\uDF89"
                : "Chào mừng bạn đến với Job Portal \uD83C\uDF89";
        String content = isEnglish
                ? """
                <p>Hello,</p>
                <p>Your account has been successfully created on our platform.</p>
                <p>Here is your temporary login password:</p>
                <div class="highlight-box">%s</div>
                <p>Please log in and change your password as soon as possible to keep your account secure.</p>
                """.formatted(generatedPassword)
                : """
                <p>Xin chào,</p>
                <p>Tài khoản của bạn đã được tạo thành công trên hệ thống của chúng tôi.</p>
                <p>Dưới đây là mật khẩu đăng nhập tạm thời của bạn:</p>
                <div class="highlight-box">%s</div>
                <p>Vui lòng đăng nhập và đổi mật khẩu sớm nhất có thể để đảm bảo bảo mật tài khoản.</p>
                """.formatted(generatedPassword);

        String htmlBody = buildHtmlTemplate(title, content, isEnglish);
        sendHtmlEmail(toEmail, subject, htmlBody);
    }

    public void sendPasswordEmail(String toEmail, String generatedPassword) {
        sendPasswordEmail(toEmail, generatedPassword, "vi");
    }

    // =======================================================================
    // 2. EMAIL QUÊN MẬT KHẨU
    // =======================================================================
    public void sendResetPasswordEmail(String toEmail, String newPassword, String lang) {
        boolean isEnglish = "en".equalsIgnoreCase(lang);
        String subject = isEnglish
                ? "JobPortal - Password Reset Request"
                : "JobPortal - Yêu cầu Cấp lại Mật khẩu";
        String title = isEnglish
                ? "Password Reset Request \uD83D\uDD12"
                : "Yêu cầu cấp lại mật khẩu \uD83D\uDD12";
        String content = isEnglish
                ? """
                <p>Hello,</p>
                <p>We received a password reset request for the account associated with this email address.</p>
                <p>Here is your new password:</p>
                <div class="highlight-box">%s</div>
                <p>Please use this password to log in. After logging in, go to <b>Profile</b> to <b>CHANGE YOUR PASSWORD</b> for security.</p>
                <p><i>If you did not make this request, please ignore this email or contact Support immediately.</i></p>
                """.formatted(newPassword)
                : """
                <p>Xin chào,</p>
                <p>Chúng tôi nhận được yêu cầu cấp lại mật khẩu cho tài khoản liên kết với địa chỉ email này.</p>
                <p>Đây là mật khẩu mới của bạn:</p>
                <div class="highlight-box">%s</div>
                <p>Vui lòng sử dụng mật khẩu này để đăng nhập vào hệ thống. Sau khi đăng nhập thành công, hãy vào mục <b>Hồ sơ cá nhân</b> để <b>ĐỔI MẬT KHẨU</b> nhằm đảm bảo an toàn.</p>
                <p><i>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này hoặc liên hệ với bộ phận Hỗ trợ ngay lập tức.</i></p>
                """.formatted(newPassword);

        String htmlBody = buildHtmlTemplate(title, content, isEnglish);
        sendHtmlEmail(toEmail, subject, htmlBody);
    }

    public void sendResetPasswordEmail(String toEmail, String newPassword) {
        sendResetPasswordEmail(toEmail, newPassword, "vi");
    }

    // =======================================================================
    // 3. EMAIL TÀI KHOẢN BỊ KHÓA
    // =======================================================================
    public void sendAccountLockedEmail(String toEmail, String name, String lang) {
        boolean isEnglish = "en".equalsIgnoreCase(lang);
        String subject = isEnglish
                ? "SECURITY WARNING: Your account has been suspended - JobPortal"
                : "CẢNH BÁO: Tài khoản của bạn đã bị khóa - JobPortal";
        String title = isEnglish
                ? "SECURITY WARNING ⚠️"
                : "CẢNH BÁO BẢO MẬT ⚠️";
        String content = isEnglish
                ? """
                <p>Hello <b>%s</b>,</p>
                <p style="color: #dc2626; font-weight: bold;">We would like to inform you that your JobPortal account has been suspended by an Administrator.</p>
                <p><b>Reason:</b> Violation of community policies or suspicious activity detected.</p>
                <p>Currently, you will not be able to log in or use our services.</p>
                <p>If you believe this is a mistake, please reach out directly to our Support team for assistance.</p>
                """.formatted(name)
                : """
                <p>Xin chào <b>%s</b>,</p>
                <p style="color: #dc2626; font-weight: bold;">Chúng tôi xin thông báo rằng tài khoản của bạn trên nền tảng JobPortal đã bị tạm khóa bởi Quản trị viên.</p>
                <p><b>Lý do:</b> Vi phạm chính sách cộng đồng hoặc phát hiện hoạt động bất thường.</p>
                <p>Hiện tại, bạn sẽ không thể đăng nhập và sử dụng các dịch vụ của chúng tôi.</p>
                <p>Nếu bạn cho rằng đây là một sự nhầm lẫn, vui lòng liên hệ trực tiếp với bộ phận Hỗ trợ (Support) để được giải quyết.</p>
                """.formatted(name);

        String htmlBody = buildHtmlTemplate(title, content, isEnglish);
        sendHtmlEmail(toEmail, subject, htmlBody);
    }

    public void sendAccountLockedEmail(String toEmail, String name) {
        sendAccountLockedEmail(toEmail, name, "vi");
    }

    // =======================================================================
    // 4. EMAIL DOANH NGHIỆP ĐƯỢC PHÊ DUYỆT
    // =======================================================================
    public void sendCompanyApprovedEmail(String toEmail, String companyName, String lang) {
        boolean isEnglish = "en".equalsIgnoreCase(lang);
        String subject = isEnglish
                ? "JobPortal - Business Profile Approved!"
                : "JobPortal - Hồ sơ doanh nghiệp đã được phê duyệt!";
        String title = isEnglish
                ? "Business Profile Approved! ✅"
                : "Hồ sơ doanh nghiệp đã được phê duyệt! ✅";
        String btnText = isEnglish ? "Go to Employer Dashboard" : "Truy cập Employer Dashboard";
        String content = isEnglish
                ? """
                <p>Hello,</p>
                <p>Congratulations! The business registration profile for company <b>'%s'</b> has been successfully approved by our Administrator.</p>
                <p>You can now access your Employer Dashboard to start utilizing our AI features, posting job listings, and hiring top talents.</p>
                <div style="text-align: center; margin: 25px 0;">
                    <a href="http://localhost:5173/employer/dashboard" class="btn" style="display: inline-block; padding: 12px 25px; background-color: #16a34a; color: #ffffff !important; text-decoration: none; border-radius: 6px; font-weight: bold;">%s</a>
                </div>
                <p>If you have any questions or need support, please do not hesitate to contact us.</p>
                """.formatted(companyName, btnText)
                : """
                <p>Xin chào,</p>
                <p>Chúc mừng! Hồ sơ đăng ký doanh nghiệp của công ty <b>'%s'</b> đã được Quản trị viên của chúng tôi phê duyệt thành công.</p>
                <p>Ngay bây giờ, bạn có thể truy cập vào Hệ thống quản trị (Employer Dashboard) để bắt đầu sử dụng các tính năng AI, đăng tải tin tuyển dụng và tìm kiếm những ứng viên tài năng nhất.</p>
                <div style="text-align: center; margin: 25px 0;">
                    <a href="http://localhost:5173/employer/dashboard" class="btn" style="display: inline-block; padding: 12px 25px; background-color: #16a34a; color: #ffffff !important; text-decoration: none; border-radius: 6px; font-weight: bold;">%s</a>
                </div>
                <p>Nếu cần hỗ trợ thêm, vui lòng liên hệ với chúng tôi.</p>
                """.formatted(companyName, btnText);

        String htmlBody = buildHtmlTemplate(title, content, isEnglish);
        sendHtmlEmail(toEmail, subject, htmlBody);
    }

    public void sendCompanyApprovedEmail(String toEmail, String companyName) {
        sendCompanyApprovedEmail(toEmail, companyName, "vi");
    }

    // =======================================================================
    // 5. EMAIL CÔNG TY BỊ TỪ CHỐI
    // =======================================================================
    public void sendCompanyRejectedEmail(String toEmail, String companyName, String lang) {
        boolean isEnglish = "en".equalsIgnoreCase(lang);
        String subject = isEnglish
                ? "JobPortal - Notice Regarding Company Profile"
                : "JobPortal - Thông báo về hồ sơ doanh nghiệp";
        String title = isEnglish
                ? "Notice Regarding Company Profile ❌"
                : "Thông báo về hồ sơ doanh nghiệp ❌";
        String content = isEnglish
                ? """
                <p>Hello,</p>
                <p>Thank you for submitting the business profile for company <b>'%s'</b> on our platform.</p>
                <p>However, after review, we regret to inform you that your company profile does not currently meet our requirements or contains incomplete information.</p>
                <p>Please log in to the system, review and update your company details (Name, Address, Tax Code, Logo, etc.) accurately.</p>
                <p><i>Once updated, the system will automatically re-submit the approval request to the Administrator.</i></p>
                """.formatted(companyName)
                : """
                <p>Xin chào,</p>
                <p>Cảm ơn bạn đã đăng ký hồ sơ doanh nghiệp cho công ty <b>'%s'</b> trên nền tảng của chúng tôi.</p>
                <p>Tuy nhiên, sau khi xem xét, chúng tôi rất tiếc phải thông báo rằng hồ sơ công ty của bạn hiện chưa đáp ứng đủ các tiêu chuẩn hoặc thông tin chưa rõ ràng để được phê duyệt.</p>
                <p>Vui lòng đăng nhập lại hệ thống, kiểm tra và cập nhật lại thông tin công ty (Tên, Địa chỉ, Mã số thuế, Logo...) sao cho chính xác nhất.</p>
                <p><i>Sau khi cập nhật, hệ thống sẽ tự động gửi lại yêu cầu phê duyệt cho Quản trị viên.</i></p>
                """.formatted(companyName);

        String htmlBody = buildHtmlTemplate(title, content, isEnglish);
        sendHtmlEmail(toEmail, subject, htmlBody);
    }

    public void sendCompanyRejectedEmail(String toEmail, String companyName) {
        sendCompanyRejectedEmail(toEmail, companyName, "vi");
    }
}

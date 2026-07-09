package com.dtn.apply_job.service;

import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.repository.UserRepository;
import com.dtn.apply_job.security.SecurityUtil;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class GmailOAuthService {

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    private final UserRepository userRepository;

    // Tận dụng JavaMailSender chỉ để "nặn" ra cái khung MimeMessage, chứ KHÔNG dùng nó để gửi
    private final JavaMailSender javaMailSender;

    public GmailOAuthService(UserRepository userRepository, JavaMailSender javaMailSender) {
        this.userRepository = userRepository;
        this.javaMailSender = javaMailSender;
    }

    // =======================================================================
    // 🎨 HÀM TẠO KHUNG GIAO DIỆN CHUNG CHUẨN ĐẸP (HTML TEMPLATE)
    // =======================================================================
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
                        .highlight-box { background-color: #f8fafc; border: 1px dashed #cbd5e1; padding: 15px; text-align: left; border-radius: 8px; margin: 20px 0; font-size: 16px; color: #374151;}
                        .footer { background-color: #f8fafc; padding: 20px; text-align: center; color: #64748b; font-size: 13px; border-top: 1px solid #e5e7eb; }
                    </style>
                </head>
                <body>
                    <div class="email-container">
                        <div class="header">
                            <h1>Job Portal</h1>
                        </div>
                        <div class="content">
                            <h2 style="color: #111827; font-size: 20px; margin-top: 0; border-bottom: 2px solid #e5e7eb; padding-bottom: 10px;">%s</h2>
                            %s
                        </div>
                        <div class="footer">
                            <p>© 2026 Job Portal Platform.</p>
                            <p>Đây là email được gửi trực tiếp từ Nhà tuyển dụng thông qua nền tảng Job Portal.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(title, mainContent);
    }

    // =======================================================================
    // 1. NHẬN CODE TỪ FRONTEND, ĐỔI LẤY REFRESH TOKEN VÀ LƯU VÀO DB
    // =======================================================================
    public void linkGoogleAccount(String authCode) throws Exception {
        String email = SecurityUtil.getCurrentUser().orElseThrow();
        User user = userRepository.findByEmail(email);

        TokenResponse response = new GoogleAuthorizationCodeTokenRequest(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                clientId,
                clientSecret,
                authCode,
                "postmessage"
        ).execute();

        String refreshToken = response.getRefreshToken();
        if (refreshToken != null) {
            user.setGoogleRefreshToken(refreshToken);
            userRepository.save(user);
        }
    }

    // =======================================================================
    // 2. HÀM CỐT LÕI: GỬI EMAIL BẰNG API GMAIL CỦA HR (OAUTH2)
    // =======================================================================
    private void sendEmailOnBehalfOfHR(User hrUser, String toEmail, String subject, String htmlContent) throws Exception {
        if (hrUser.getGoogleRefreshToken() == null) {
            throw new Exception("HR chưa liên kết tài khoản Gmail!");
        }

        // Lấy lại chìa khóa từ Google bằng Refresh Token lưu trong DB
        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                .setJsonFactory(GsonFactory.getDefaultInstance())
                .setClientSecrets(clientId, clientSecret)
                .build()
                .setRefreshToken(hrUser.getGoogleRefreshToken());

        Gmail gmail = new Gmail.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), credential)
                .setApplicationName("JobPortal")
                .build();

        // Nặn bức thư HTML thành MimeMessage
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        // Chuyển bức thư sang chuẩn Base64URL để Google API có thể đọc được
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        mimeMessage.writeTo(buffer);
        byte[] rawMessageBytes = buffer.toByteArray();
        String encodedEmail = Base64.getUrlEncoder().encodeToString(rawMessageBytes);

        Message message = new Message();
        message.setRaw(encodedEmail);

        // 🚀 Phát lệnh bắn email đi từ tài khoản của HR
        gmail.users().messages().send("me", message).execute();
    }

    // =======================================================================
    // 3. GỬI THƯ MỜI PHỎNG VẤN (TỰ ĐỘNG SINH HTML VÀ GỌI HÀM OAUTH2 Ở TRÊN)
    // =======================================================================
    @Async // Đẩy tiến trình này chạy ngầm để web của HR không bị lag
    public void sendInterviewInvitationAsync(User hrUser, String toEmail, String candidateName, String companyName, String jobTitle, String time, String location, String hrMessage) {
        try {
            String title = "Thư mời phỏng vấn - " + companyName + " \uD83D\uDCE7";
            String content = """
                    <p>Chào <b>%s</b>,</p>
                    <p>Cảm ơn bạn đã quan tâm và ứng tuyển vào vị trí <b>%s</b> tại <b>%s</b>.</p>
                    <p>Sau khi xem xét hồ sơ, chúng tôi rất ấn tượng với những kỹ năng của bạn và trân trọng kính mời bạn tham gia buổi phỏng vấn.</p>
                    
                    <div class="highlight-box">
                        <p style="margin: 5px 0;">⏰ <b>Thời gian:</b> <span style="color: #16a34a; font-weight: bold;">%s</span></p>
                        <p style="margin: 5px 0;">📍 <b>Địa điểm / Link Online:</b> <a href="%s" style="color: #2563eb; text-decoration: underline;" target="_blank">%s</a></p>
                    </div>
                    
                    <p><b>Lời nhắn từ Nhà tuyển dụng:</b></p>
                    <blockquote style="border-left: 4px solid #16a34a; padding-left: 15px; color: #4b5563; font-style: italic; background: #f0fdf4; padding: 10px;">
                        "%s"
                    </blockquote>
                    
                    <p>Vui lòng phản hồi lại email này để xác nhận khả năng tham dự của bạn. Chúc bạn có một buổi phỏng vấn thành công!</p>
                    """.formatted(candidateName, jobTitle, companyName, time, location, location, hrMessage);

            String htmlBody = buildHtmlTemplate(title, content);
            String subject = "Thư mời phỏng vấn: " + jobTitle + " - " + companyName;

            // 🚀 Bắn thư thông qua tài khoản thật của HR (OAuth2)
            sendEmailOnBehalfOfHR(hrUser, toEmail, subject, htmlBody);
            System.out.println(">>> Đã gửi Thư mời phỏng vấn bằng Gmail cá nhân của HR tới: " + toEmail);

        } catch (Exception e) {
            System.err.println(">>> Lỗi khi dùng Gmail API gửi thư phỏng vấn: " + e.getMessage());
        }
    }
}
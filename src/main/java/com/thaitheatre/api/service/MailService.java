package com.thaitheatre.api.service;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {

    private static final String SUPPORT_EMAIL = "support@thaitheatre.org";

    private final JavaMailSender mailSender;

    // กำหนดค่า default "from" จาก application.properties ก็ได้
    @Value("${spring.mail.from:no-reply@thaitheatre.org}")
    private String fromAddress;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcome(String toEmail, String fullName) {
        String name = safeName(fullName);
        String subject = "🎭 ยินดีต้อนรับสู่ THAI ART HUB";
        String text = """
                สวัสดีคุณ %s,

                ขอบคุณที่สมัครเข้าร่วมกับ THAI ART HUB
                บัญชีของคุณถูกสร้างเรียบร้อยแล้ว และพร้อมใช้งานทันที

                หากคุณไม่ได้เป็นผู้ดำเนินการ กรุณาติดต่อผู้ดูแลระบบที่ %s

                ขอแสดงความนับถือ,
                Thai Theatre Team
                """.formatted(name, SUPPORT_EMAIL);

        String body = """
                <h1 style="margin:0 0 8px;font-size:22px;color:#15302b;">ยินดีต้อนรับ 🎭</h1>
                <p style="margin:0 0 14px;font-size:14px;line-height:1.8;color:#4a5a55;">
                  สวัสดีคุณ <strong style="color:#15302b;">%s</strong>,<br>
                  ขอบคุณที่สมัครเข้าร่วมกับ <strong>THAI ART HUB</strong>
                  บัญชีของคุณถูกสร้างเรียบร้อยแล้ว และพร้อมใช้งานทันที
                </p>
                <p style="margin:0;font-size:13px;line-height:1.8;color:#4a5a55;">
                  เริ่มสร้างโปรไฟล์ แบ่งปันบทละคร และค้นหาโอกาสในการทำงานได้เลย
                </p>
                """.formatted(name);

        sendHtml(toEmail, subject, text, buildBrandedHtml("ยินดีต้อนรับ", body, "#147B7E"));
    }

    public void sendAccountDeleted(String toEmail, String fullName) {
        String name = safeName(fullName);
        String subject = "บัญชีของคุณถูกลบเรียบร้อยแล้ว · THAI ART HUB";
        String text = """
                สวัสดีคุณ %s,

                บัญชีของคุณในระบบ THAI ART HUB ถูกลบเรียบร้อยแล้วตามคำร้องขอของคุณ
                ข้อมูลของคุณจะถูกปิดการใช้งานตามนโยบายความเป็นส่วนตัว

                หากคุณไม่ได้เป็นผู้ดำเนินการ กรุณาติดต่อผู้ดูแลระบบทันทีที่ %s

                ขอแสดงความนับถือ,
                Thai Theatre Team
                """.formatted(name, SUPPORT_EMAIL);

        String body = """
                <h1 style="margin:0 0 8px;font-size:22px;color:#8f382c;">บัญชีถูกลบแล้ว</h1>
                <p style="margin:0 0 14px;font-size:14px;line-height:1.8;color:#4a5a55;">
                  สวัสดีคุณ <strong style="color:#15302b;">%s</strong>,<br>
                  บัญชีของคุณในระบบ THAI ART HUB ถูกลบเรียบร้อยแล้วตามคำร้องขอของคุณ
                  ข้อมูลของคุณจะถูกปิดการใช้งานตามนโยบายความเป็นส่วนตัว
                </p>
                <div style="margin-top:4px;padding:12px 14px;border-radius:12px;background:#fff0ed;border:1px solid #efc4b8;">
                  <p style="margin:0;font-size:12px;line-height:1.7;color:#8f382c;">
                    หากคุณ <strong>ไม่ได้</strong> เป็นผู้ดำเนินการ กรุณาติดต่อผู้ดูแลระบบทันทีที่
                    <a href="mailto:%s" style="color:#8f382c;font-weight:700;">%s</a>
                  </p>
                </div>
                """.formatted(name, SUPPORT_EMAIL, SUPPORT_EMAIL);

        sendHtml(toEmail, subject, text, buildBrandedHtml("บัญชีถูกลบ", body, "#8f382c"));
    }

    public void sendEmailChanged(String oldEmail, String newEmail, String fullName) {
        String name = safeName(fullName);
        String subject = "มีการเปลี่ยนอีเมลบัญชีของคุณ · THAI ART HUB";
        String text = """
                สวัสดีคุณ %s,

                คุณได้เปลี่ยนอีเมลสำหรับบัญชี THAI ART HUB ของคุณ
                อีเมลใหม่คือ: %s

                หากคุณไม่ได้เปลี่ยนอีเมลด้วยตนเอง กรุณาติดต่อผู้ดูแลระบบทันทีที่ %s

                ขอแสดงความนับถือ,
                Thai Theatre Team
                """.formatted(name, newEmail, SUPPORT_EMAIL);

        String body = """
                <h1 style="margin:0 0 8px;font-size:22px;color:#15302b;">เปลี่ยนอีเมลสำเร็จ</h1>
                <p style="margin:0 0 14px;font-size:14px;line-height:1.8;color:#4a5a55;">
                  สวัสดีคุณ <strong style="color:#15302b;">%s</strong>,<br>
                  คุณได้เปลี่ยนอีเมลสำหรับบัญชี THAI ART HUB ของคุณ
                </p>
                <div style="margin:0 0 16px;padding:12px 14px;border-radius:12px;background:#f0f6f1;">
                  <div style="font-size:10px;font-weight:800;letter-spacing:1px;color:#5b817c;text-transform:uppercase;">อีเมลใหม่</div>
                  <div style="font-size:15px;font-weight:700;color:#15302b;margin-top:2px;">%s</div>
                </div>
                <div style="padding:12px 14px;border-radius:12px;background:#fff0ed;border:1px solid #efc4b8;">
                  <p style="margin:0;font-size:12px;line-height:1.7;color:#8f382c;">
                    หากคุณ <strong>ไม่ได้</strong> เปลี่ยนอีเมลด้วยตนเอง กรุณาติดต่อผู้ดูแลระบบทันทีที่
                    <a href="mailto:%s" style="color:#8f382c;font-weight:700;">%s</a>
                  </p>
                </div>
                """.formatted(name, newEmail, SUPPORT_EMAIL, SUPPORT_EMAIL);

        // แจ้งทางอีเมลเก่าก่อน (กันบัญชีถูกยึด)
        sendHtml(oldEmail, subject, text, buildBrandedHtml("เปลี่ยนอีเมล", body, "#147B7E"));
    }

    // ---------- helpers ----------

    private String safeName(String fullName) {
        return (fullName == null || fullName.isBlank()) ? "คุณผู้ใช้" : fullName.trim();
    }

    /** Send a multipart (plain-text + HTML) email; fall back to plain text on error. */
    private void sendHtml(String toEmail, String subject, String textFallback, String html) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());
            helper.setTo(toEmail);
            helper.setFrom(String.format("THAI ART HUB <%s>", fromAddress));
            helper.setSubject(subject);
            helper.setText(textFallback, html);
            mailSender.send(mime);
        } catch (MessagingException e) {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(toEmail);
            msg.setFrom(String.format("THAI ART HUB <%s>", fromAddress));
            msg.setSubject(subject);
            msg.setText(textFallback);
            mailSender.send(msg);
        }
    }

    /** Shared branded shell. `accent` colours the header bar. */
    private String buildBrandedHtml(String preheader, String bodyHtml, String accent) {
        return """
                <!DOCTYPE html>
                <html lang="th">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin:0;padding:0;background:#eef3ef;font-family:'Segoe UI',Tahoma,Arial,sans-serif;">
                  <span style="display:none;max-height:0;overflow:hidden;opacity:0;">%s</span>
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#eef3ef;padding:28px 12px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:520px;background:#ffffff;border-radius:18px;overflow:hidden;box-shadow:0 10px 30px rgba(20,123,126,0.12);">
                          <tr>
                            <td style="background:%s;padding:26px 32px;">
                              <div style="color:#ffffff;font-size:20px;font-weight:800;letter-spacing:0.5px;">THAI ART HUB</div>
                              <div style="color:#dbeeeb;font-size:11px;letter-spacing:2px;margin-top:2px;">PERFORMING ARTS · PROFILES · SCRIPTS</div>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:32px;">
                              %s
                              <hr style="border:none;border-top:1px solid #e5ece8;margin:24px 0;">
                              <p style="margin:0;font-size:12px;line-height:1.7;color:#9aa8a3;">
                                ขอแสดงความนับถือ,<br>Thai Theatre Team
                              </p>
                            </td>
                          </tr>
                          <tr>
                            <td style="background:#f4f8f6;padding:18px 32px;text-align:center;">
                              <div style="font-size:11px;color:#9aa8a3;">© THAI ART HUB · มูลนิธิไทยเธียเตอร์</div>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(preheader, accent, bodyHtml);
    }

}

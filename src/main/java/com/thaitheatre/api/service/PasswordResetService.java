package com.thaitheatre.api.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.thaitheatre.api.common.AccountDeletedException;
import com.thaitheatre.api.common.DelFlag;
import com.thaitheatre.api.common.RecordStatus;
import com.thaitheatre.api.model.entity.PasswordResetToken;
import com.thaitheatre.api.model.entity.UserAccount;
import com.thaitheatre.api.repository.PasswordResetTokenRepository;
import com.thaitheatre.api.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepo;
    private final UserRepository userRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    // ทำให้ mailSender เป็น optional: ถ้าไม่มี starter-mail ก็ไม่ล้ม
    private final JavaMailSender mailSender;

    private final SecureRandom secureRandom = new SecureRandom();
    private static final int TOKEN_BYTES = 32; // 32 bytes -> base64url ~43 chars

    @Value("${app.reset-token.exp-minutes:60}")
    private long tokenExpiryMinutes;

    public PasswordResetService(
            PasswordResetTokenRepository tokenRepo,
            UserRepository userRepo,
            BCryptPasswordEncoder passwordEncoder,
            ObjectProvider<JavaMailSender> mailSenderProvider // optional
    ) {
        this.tokenRepo = tokenRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSenderProvider.getIfAvailable(); // อาจเป็น null ได้
    }

    // generate token, save hashed version, send email
    // import java.util.Optional;
    public Optional<String> requestPasswordReset(String email, String appBaseUrl) {
        return userRepo.findByEmail(email.toLowerCase()).map(user -> {
            // บัญชีที่ถูกลบ (soft delete) ไม่ควรรีเซ็ตรหัสผ่าน แต่ก็ไม่ควรขึ้นว่า "ไม่พบอีเมล"
            // เพราะอีเมลนั้นมีอยู่จริง → แจ้งว่าบัญชีถูกลบและให้ลงทะเบียนใหม่.
            if (user.getDelFlag() == DelFlag.Y || user.getRecordStatus() == RecordStatus.I) {
                throw new AccountDeletedException(
                        "This account has been deleted. Please register again.");
            }

            // Invalidate any previous outstanding tokens so only the newest link works.
            tokenRepo.markAllUsedForUser(user);

            String token = generateToken();
            String tokenHash = hashToken(token);
            Instant expiresAt = Instant.now().plus(tokenExpiryMinutes, ChronoUnit.MINUTES);

            PasswordResetToken prt = new PasswordResetToken();
            prt.setUser(user);
            prt.setTokenHash(tokenHash);
            prt.setExpiresAt(expiresAt);
            prt.setUsed(false);
            tokenRepo.save(prt);

            String resetUrl = appBaseUrl
                    + "/auth/new-password"
                    + "?token=" + urlEncode(token)
                    + "&email=" + urlEncode(email);

            sendResetEmail(user.getEmail(), user.getFirstName(), resetUrl);

            // คืนค่าคีย์ (raw token) ให้ controller ถ้าต้องการเอาไปส่งต่อ
            return token;
        });
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // ✅ เมธอดที่ขาดไป
    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            // ไม่ควรเกิดกับ UTF-8
            throw new RuntimeException("URL encoding failed", e);
        }
    }

    private void sendResetEmail(String toEmail, String name, String resetUrl) {
        String subject = "รีเซ็ตรหัสผ่าน · THAI ART HUB";
        String displayName = (name == null || name.isBlank()) ? "คุณ" : name;

        // Plain-text fallback (for clients that don't render HTML + better deliverability).
        String text = String.format("""
                สวัสดี %s,

                เราได้รับคำขอรีเซ็ตรหัสผ่านสำหรับบัญชีของคุณที่ THAI ART HUB
                หากเป็นคำขอจากคุณ เปิดลิงก์ด้านล่างเพื่อตั้งรหัสผ่านใหม่ (ลิงก์หมดอายุใน %d นาที):

                %s

                หากคุณไม่ได้เป็นผู้ร้องขอ สามารถเพิกเฉยอีเมลฉบับนี้ได้ รหัสผ่านของคุณจะไม่เปลี่ยนแปลง

                — THAI ART HUB
                """, displayName, tokenExpiryMinutes, resetUrl);

        String html = buildResetEmailHtml(displayName, resetUrl, tokenExpiryMinutes);

        if (mailSender == null) {
            // fallback: log ไว้ก่อน ถ้ายังไม่ได้ตั้งค่าเมล
            System.out.printf("[MAIL-FAKE] to=%s subject=%s body=%s%n", toEmail, subject, text);
            return;
        }

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(text, html); // (plainText, htmlText) -> multipart/alternative
            mailSender.send(mime);
        } catch (MessagingException e) {
            // ถ้าสร้าง/ส่ง HTML ไม่ได้ ให้ fallback เป็น plain text แทนที่จะล้มเงียบ ๆ
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        }
    }

    /** Branded, responsive HTML for the password-reset email. */
    private String buildResetEmailHtml(String name, String resetUrl, long expiryMinutes) {
        return """
                <!DOCTYPE html>
                <html lang="th">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin:0;padding:0;background:#eef3ef;font-family:'Segoe UI',Tahoma,Arial,sans-serif;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#eef3ef;padding:28px 12px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:520px;background:#ffffff;border-radius:18px;overflow:hidden;box-shadow:0 10px 30px rgba(20,123,126,0.12);">
                          <!-- Header -->
                          <tr>
                            <td style="background:#147B7E;padding:26px 32px;">
                              <div style="color:#ffffff;font-size:20px;font-weight:800;letter-spacing:0.5px;">THAI ART HUB</div>
                              <div style="color:#bfe3df;font-size:11px;letter-spacing:2px;margin-top:2px;">PERFORMING ARTS · PROFILES · SCRIPTS</div>
                            </td>
                          </tr>
                          <!-- Body -->
                          <tr>
                            <td style="padding:32px;">
                              <h1 style="margin:0 0 8px;font-size:22px;color:#15302b;">รีเซ็ตรหัสผ่าน</h1>
                              <p style="margin:0 0 18px;font-size:14px;line-height:1.8;color:#4a5a55;">
                                สวัสดี <strong style="color:#15302b;">%s</strong>,<br>
                                เราได้รับคำขอรีเซ็ตรหัสผ่านสำหรับบัญชีของคุณ หากเป็นคำขอจากคุณ
                                กดปุ่มด้านล่างเพื่อตั้งรหัสผ่านใหม่
                              </p>
                              <!-- CTA -->
                              <table role="presentation" cellpadding="0" cellspacing="0" style="margin:22px 0;">
                                <tr>
                                  <td style="border-radius:999px;background:#147B7E;">
                                    <a href="%s" target="_blank"
                                       style="display:inline-block;padding:13px 30px;font-size:15px;font-weight:700;color:#ffffff;text-decoration:none;border-radius:999px;">
                                      ตั้งรหัสผ่านใหม่
                                    </a>
                                  </td>
                                </tr>
                              </table>
                              <p style="margin:0 0 6px;font-size:12px;color:#7b8c86;">
                                ลิงก์นี้จะหมดอายุใน <strong>%d นาที</strong>
                              </p>
                              <p style="margin:18px 0 0;font-size:12px;line-height:1.7;color:#7b8c86;">
                                หากปุ่มกดไม่ได้ ให้คัดลอกลิงก์นี้ไปวางในเบราว์เซอร์:<br>
                                <a href="%s" style="color:#147B7E;word-break:break-all;">%s</a>
                              </p>
                              <hr style="border:none;border-top:1px solid #e5ece8;margin:24px 0;">
                              <p style="margin:0;font-size:12px;line-height:1.7;color:#9aa8a3;">
                                หากคุณไม่ได้เป็นผู้ร้องขอ สามารถเพิกเฉยอีเมลฉบับนี้ได้
                                รหัสผ่านของคุณจะไม่เปลี่ยนแปลง
                              </p>
                            </td>
                          </tr>
                          <!-- Footer -->
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
                """.formatted(name, resetUrl, expiryMinutes, resetUrl, resetUrl);
    }

    // validate token by hashing candidate and comparing to stored hash
    public boolean validateTokenForEmail(String email, String token) {
        Optional<UserAccount> ou = userRepo.findByEmail(email.toLowerCase());
        if (ou.isEmpty()) {
            return false;
        }

        UserAccount user = ou.get();
        Optional<PasswordResetToken> opt
                = tokenRepo.findByUserAndUsedFalseAndExpiresAtAfter(user, Instant.now());
        if (opt.isEmpty()) {
            return false;
        }

        PasswordResetToken prt = opt.get();
        String candidateHash = hashToken(token);
        return candidateHash.equalsIgnoreCase(prt.getTokenHash());
    }

    @Transactional
    public boolean resetPassword(String email, String token, String newPassword) {
        if (token == null || token.isBlank()) {
            return false;
        }

        String candidateHash = hashToken(token);

        // หา token จาก hash โดยตรง (hash ไม่ซ้ำ จึงได้ใบที่ตรงกับลิงก์เสมอ แม้ user
        // จะมี active token หลายใบ). วิธีเดิมที่หาโดย user แล้วค่อยเทียบ hash จะพังเมื่อ
        // มีหลายใบ เพราะ query คืน Optional เดียวแบบไม่แน่นอน.
        Optional<PasswordResetToken> opt
                = tokenRepo.findByTokenHashAndUsedFalseAndExpiresAtAfter(candidateHash, Instant.now());
        if (opt.isEmpty()) {
            return false;
        }

        PasswordResetToken prt = opt.get();
        UserAccount user = prt.getUser();

        // ถ้าส่ง email มาด้วย ให้ยืนยันว่า token เป็นของ user คนนั้นจริง
        if (email != null && !email.isBlank()) {
            if (user == null || user.getEmail() == null
                    || !user.getEmail().equalsIgnoreCase(email.trim())) {
                return false;
            }
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        prt.setUsed(true);
        tokenRepo.save(prt);

        return true;
    }
}

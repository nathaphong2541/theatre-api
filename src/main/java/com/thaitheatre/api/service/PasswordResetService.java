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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
    public void requestPasswordReset(String email, String appBaseUrl) {
        userRepo.findByEmail(email.toLowerCase()).ifPresent(user -> {
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
                    + "/reset-password?token=" + urlEncode(token)
                    + "&email=" + urlEncode(email);

            sendResetEmail(user.getEmail(), user.getFirstName(), resetUrl);
        });
        // ไม่บอกว่าอีเมลมี/ไม่มี เพื่อความปลอดภัย
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
        String subject = "ลิงก์รีเซ็ตรหัสผ่านของคุณ";
        String text = String.format("""
                สวัสดี %s,

                ได้รับคำขอรีเซ็ตรหัสผ่านสำหรับบัญชีของคุณ หากเป็นคำขอจากคุณ ให้คลิกที่ลิงก์ด้านล่างเพื่อตั้งรหัสผ่านใหม่ (ลิงก์จะหมดอายุใน %d นาที):
                %s

                หากคุณไม่ได้ร้องขอ กรุณาไม่ดำเนินการใด ๆ
                """, name, tokenExpiryMinutes, resetUrl);

        if (mailSender != null) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } else {
            // fallback: log ไว้ก่อน ถ้ายังไม่ได้ตั้งค่าเมล
            System.out.printf("[MAIL-FAKE] to=%s subject=%s body=%s%n", toEmail, subject, text);
        }
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
        if (!candidateHash.equalsIgnoreCase(prt.getTokenHash())) {
            return false;
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        prt.setUsed(true);
        tokenRepo.save(prt);
        return true;
    }
}

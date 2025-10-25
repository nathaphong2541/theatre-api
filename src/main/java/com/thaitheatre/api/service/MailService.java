package com.thaitheatre.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    // กำหนดค่า default "from" จาก application.properties ก็ได้
    @Value("${spring.mail.from:no-reply@thaitheatre.org}")
    private String fromAddress;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcome(String toEmail, String fullName) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setFrom(String.format("Thai Theatre Team <%s>", fromAddress));
        msg.setSubject("🎭 ยินดีต้อนรับสู่ Thai Theatre!");

        msg.setText("""
                สวัสดีคุณ %s,

                ขอบคุณที่สมัครเข้าร่วมกับ Thai Theatre 🎭
                บัญชีของคุณได้ถูกสร้างเรียบร้อยแล้ว และพร้อมให้คุณใช้งานทันที

                หากคุณไม่ได้เป็นผู้ดำเนินการ กรุณาติดต่อผู้ดูแลระบบโดยเร็วที่
                support@thaitheatre.org

                ขอแสดงความนับถือ,
                Thai Theatre Team
                """.formatted(fullName));

        mailSender.send(msg);
    }
}

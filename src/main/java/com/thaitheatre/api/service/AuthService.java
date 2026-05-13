package com.thaitheatre.api.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.thaitheatre.api.common.DelFlag;
import com.thaitheatre.api.common.EmailAlreadyUsedException;
import com.thaitheatre.api.common.InvalidCredentialException;
import com.thaitheatre.api.common.RecordStatus;
import com.thaitheatre.api.model.dto.AuthResponse;
import com.thaitheatre.api.model.dto.LoginRequest;
import com.thaitheatre.api.model.dto.RegisterRequest;
import com.thaitheatre.api.model.dto.UserProfileDTO;
import com.thaitheatre.api.model.entity.UserAccount;
import com.thaitheatre.api.model.enums.UserRole;
import com.thaitheatre.api.repository.UserRepository;
import com.thaitheatre.api.security.JwtUtil;

@Service
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwt;
    private final MailService mailService;

    public AuthService(UserRepository repo, PasswordEncoder encoder,
            AuthenticationManager authManager, JwtUtil jwt, MailService mailService) {
        this.repo = repo;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwt = jwt;
        this.mailService = mailService;
    }

    public UserProfileDTO register(RegisterRequest rq) {
        if (repo.existsByEmail(rq.email().trim().toLowerCase())) {
            throw new EmailAlreadyUsedException();
        }
        UserAccount u = new UserAccount();
        u.setFirstName(rq.firstName().trim());
        u.setLastName(rq.lastName().trim());
        u.setRole(UserRole.USER);
        u.setEmail(rq.email().trim().toLowerCase());
        u.setPasswordHash(encoder.encode(rq.password()));
        u.setPolicyConfirmed(rq.policyConfirm());

        // ✅ บังคับค่าเริ่มต้นเสมอ
        u.setRecordStatus(RecordStatus.A);
        u.setDelFlag(DelFlag.N);

        repo.save(u);

        try {
            mailService.sendWelcome(u.getEmail(), u.getFirstName() + " " + u.getLastName());
        } catch (Exception ex) {
            System.err.println("Send welcome mail failed: " + ex.getMessage());
        }

        return new UserProfileDTO(
                u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getPolicyConfirmed());
    }

    public AuthResponse login(LoginRequest rq) {
        var email = rq.email().trim().toLowerCase();

        try {
            var auth = new UsernamePasswordAuthenticationToken(email, rq.password());
            authManager.authenticate(auth); // ถ้าผิด → AuthenticationException
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialException("Invalid email or password");
        }

        var user = repo.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialException("Invalid email or password"));

        String token = jwt.generate(String.valueOf(user.getId()));
        long expSec = jwt.getExpMillis() / 1000;

        return AuthResponse.bearer(
                token,
                expSec,
                new UserProfileDTO(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getPolicyConfirmed()));
    }

    public void deleteAccountByEmail(String email) {
        var user = repo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // ✅ ทำ soft-delete ตาม enum ที่คุณใช้
        user.setRecordStatus(RecordStatus.I);
        user.setDelFlag(DelFlag.Y);

        repo.save(user);

        try {
            mailService.sendAccountDeleted(user.getEmail(), user.getFirstName() + " " + user.getLastName());
        } catch (Exception ex) {
            System.err.println("Send delete-account mail failed: " + ex.getMessage());
        }
    }

    public UserProfileDTO changeEmail(String currentEmail, String newEmailRaw, String currentPassword) {
        var newEmail = newEmailRaw.trim().toLowerCase();

        // ✅ ตรวจว่าอีเมลใหม่ถูกใช้งานแล้วหรือยัง
        if (repo.existsByEmail(newEmail)) {
            throw new EmailAlreadyUsedException(); // ใช้ exception เดิมที่คุณมีอยู่
        }

        var user = repo.findByEmail(currentEmail.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // ✅ เช็ครหัสผ่านก่อนเปลี่ยนอีเมล
        if (!encoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        var oldEmail = user.getEmail();
        user.setEmail(newEmail);
        repo.save(user);

        try {
            mailService.sendEmailChanged(oldEmail, newEmail, user.getFirstName() + " " + user.getLastName());
        } catch (Exception ex) {
            System.err.println("Send change-email mail failed: " + ex.getMessage());
        }

        return new UserProfileDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPolicyConfirmed());
    }
}

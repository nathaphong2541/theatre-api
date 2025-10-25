package com.thaitheatre.api.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.thaitheatre.api.common.DelFlag;
import com.thaitheatre.api.common.EmailAlreadyUsedException;
import com.thaitheatre.api.common.RecordStatus;
import com.thaitheatre.api.model.dto.AuthResponse;
import com.thaitheatre.api.model.dto.LoginRequest;
import com.thaitheatre.api.model.dto.RegisterRequest;
import com.thaitheatre.api.model.dto.UserProfileDTO;
import com.thaitheatre.api.model.entity.UserAccount;
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
                u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getPolicyConfirmed()
        );
    }

    public AuthResponse login(LoginRequest rq) {
        // 1) auth (จะโยน exception เองถ้ารหัสผ่านผิด)
        var auth = new UsernamePasswordAuthenticationToken(
                rq.email().trim().toLowerCase(), rq.password());
        authManager.authenticate(auth);

        // 2) ดึง user จาก DB เพื่อได้ id
        var email = rq.email().trim().toLowerCase();
        var user = repo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        // 3) ออก token โดยใช้ userId เป็น subject (สำคัญ!)
        String token = jwt.generate(String.valueOf(user.getId())); // ✅ sub = userId

        System.out.println("Generated JWT Token: " + token);

        long expSec = jwt.getExpMillis() / 1000;
        return AuthResponse.bearer(token, expSec, new UserProfileDTO(
                user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getPolicyConfirmed()
        ));
    }
}

package com.thaitheatre.api.controller;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thaitheatre.api.model.dto.AuthResponse;
import com.thaitheatre.api.model.dto.ForgotPasswordRequest;
import com.thaitheatre.api.model.dto.GenericResponse;
import com.thaitheatre.api.model.dto.LoginRequest;
import com.thaitheatre.api.model.dto.RegisterRequest;
import com.thaitheatre.api.model.dto.ResetPasswordRequest;
import com.thaitheatre.api.model.dto.UserProfileDTO;
import com.thaitheatre.api.repository.UserRepository;
import com.thaitheatre.api.service.AuthService;
import com.thaitheatre.api.service.PasswordResetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String COOKIE_NAME = "access_token";

    private final AuthService auth;
    private final PasswordResetService prs;

    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBase;

    @Value("${app.cookie.secure:false}")       // prod → true
    private boolean cookieSecure;

    @Value("${app.cookie.max-age-seconds:7200}") // default 2 ชั่วโมง
    private long cookieMaxAgeSeconds;

    private final UserRepository userRepository; // <— เพิ่ม

    public AuthController(AuthService auth, PasswordResetService prs, UserRepository userRepository) {
        this.auth = auth;
        this.prs = prs;
        this.userRepository = userRepository;
    }

    // --- Register (ไม่ต้องใช้ token)
    @PostMapping("/register")
    @Operation(summary = "Register")
    public ResponseEntity<UserProfileDTO> register(@Valid @RequestBody RegisterRequest rq) {
        return ResponseEntity.ok(auth.register(rq));
    }

    // --- Login (เซ็ต JWT ลง HttpOnly Cookie)
    @PostMapping("/login")
    @Operation(summary = "Login (set HttpOnly cookie)")
    public ResponseEntity<?> login(@RequestBody LoginRequest rq, HttpServletResponse response) {
        // ให้ service ตรวจสอบ credential และคืน AuthResponse (มี token และ user)
        AuthResponse authRes = auth.login(rq);

        // สร้างคุกกี้เก็บ JWT
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, authRes.accessToken())
                .httpOnly(true)
                .secure(cookieSecure) // true เมื่ออยู่หลัง HTTPS
                .sameSite("Strict") // ถ้าต้องการ cross-site form POST อาจใช้ Lax
                .path("/")
                .maxAge(Duration.ofSeconds(cookieMaxAgeSeconds))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // ไม่จำเป็นต้องส่ง token กลับไป (ฝั่ง FE อ่านไม่ได้อยู่แล้ว)
        // ส่งข้อมูล user/ข้อความกลับไปก็พอ
        return ResponseEntity.ok(Map.of(
                "user", authRes.user(),
                "message", "Login successful"
        ));
    }

    @GetMapping("/me")
    @Operation(summary = "Test auth (requires cookie JWT)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> me() {
        var authn = SecurityContextHolder.getContext().getAuthentication();
        if (authn == null || !authn.isAuthenticated() || "anonymousUser".equals(authn.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));
        }

        String email = authn.getName(); // มาจาก UserDetails.getUsername()
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }
        var u = userOpt.get();

        return ResponseEntity.ok(Map.of(
                "email", u.getEmail(),
                "firstName", u.getFirstName(),
                "lastName", u.getLastName()
        ));
    }

    // --- Logout (ลบคุกกี้)
    @PostMapping("/logout")
    @Operation(summary = "Logout (clear HttpOnly cookie)")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie cleared = ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/")
                .maxAge(0) // expire ทันที
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cleared.toString());
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    // --- Forgot password (ไม่ต้องใช้ token)
    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset (email)")
    public ResponseEntity<GenericResponse> forgotPassword(@RequestBody ForgotPasswordRequest rq) {
        prs.requestPasswordReset(rq.email(), frontendBase);
        return ResponseEntity.ok(new GenericResponse("If an account exists, a reset link has been sent."));
    }

    // --- Reset password (ไม่ต้องใช้ token)
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password by email + token")
    public ResponseEntity<GenericResponse> resetPassword(@RequestBody ResetPasswordRequest rq) {
        boolean ok = prs.resetPassword(rq.email(), rq.token(), rq.newPassword());
        if (!ok) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GenericResponse("Invalid or expired token/email."));
        }
        return ResponseEntity.ok(new GenericResponse("Password has been reset successfully."));
    }
}

package com.thaitheatre.api.controller;

import com.thaitheatre.api.model.dto.*;
import com.thaitheatre.api.service.AuthService;
import com.thaitheatre.api.service.PasswordResetService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 👉 Swagger/OpenAPI annotations
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;
    private final PasswordResetService prs;

    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBase;

    public AuthController(AuthService auth, PasswordResetService prs) {
        this.auth = auth;
        this.prs = prs;
    }

    // --- Register (ไม่ต้องใช้ token)
    @PostMapping("/register")
    @Operation(summary = "Register")
    public ResponseEntity<UserProfileDTO> register(@Valid @RequestBody RegisterRequest rq) {
        return ResponseEntity.ok(auth.register(rq));
    }

    // --- Login (ไม่ต้องใช้ token)
    @PostMapping("/login")
    @Operation(summary = "Login (returns JWT)")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest rq) {
        return ResponseEntity.ok(auth.login(rq));
    }

    // --- Me (ต้องใช้ token) ---
    @GetMapping("/me")
    @Operation(summary = "Test auth (requires Bearer token)")
    @SecurityRequirement(name = "bearerAuth") // ✅ ให้ Swagger ใส่ Authorization header อัตโนมัติ
    public ResponseEntity<String> me(HttpServletRequest request) {
        String authz = request.getHeader(HttpHeaders.AUTHORIZATION); // "Bearer <token>"
        if (authz == null || authz.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing Authorization header");
        }

        // ถ้าต้องการดึง token:
        // String token = authz.startsWith("Bearer ") ? authz.substring(7) : authz;
        // TODO: ตรวจสอบ/ถอดรหัส token (เช่น jwtUtil.validateToken(token); jwtUtil.getUserId(token))

        return ResponseEntity.ok("OK");
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

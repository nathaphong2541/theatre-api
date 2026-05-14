package com.thaitheatre.api.model.dto.admin;

// RegisterRequest เดิมมีอยู่แล้ว

public record ResetPasswordRequest(String email, String token, String newPassword) {}

package com.thaitheatre.api.common;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", 400,
                "error", "Bad Request",
                "message", ex.getMessage()
        ));
    }

    // ถ้า login ผิด → ส่ง 401 + JSON ชัด ๆ
    @ExceptionHandler(InvalidCredentialException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredential(
            InvalidCredentialException ex,
            HttpServletRequest request
    ) {
        ErrorResponse body = new ErrorResponse(
                "INVALID_CREDENTIALS",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // บัญชีถูกลบไปแล้ว (soft delete) แต่พยายามเข้าสู่ระบบด้วยรหัสผ่านที่ถูกต้อง
    @ExceptionHandler(AccountDeletedException.class)
    public ResponseEntity<ErrorResponse> handleAccountDeleted(
            AccountDeletedException ex,
            HttpServletRequest request
    ) {
        ErrorResponse body = new ErrorResponse(
                "ACCOUNT_DELETED",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // ไม่มีสิทธิ์ทำรายการ (เช่น ลบ/แก้บทละครที่ไม่ใช่ของตัวเอง) → 403
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            org.springframework.security.access.AccessDeniedException ex,
            HttpServletRequest request
    ) {
        ErrorResponse body = new ErrorResponse(
                "FORBIDDEN",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // Bean-validation failures (@Size/@Email/@NotBlank on request DTOs) → 400 with
    // a specific code + field so the client can show a clear reason instead of a
    // generic "could not create account". Password-length failures get their own
    // code so the UI can point at the password rule.
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {
        var fieldError = ex.getBindingResult().getFieldError();
        String field = fieldError != null ? fieldError.getField() : null;
        String defaultMsg = fieldError != null ? fieldError.getDefaultMessage() : "Invalid request";

        String code = "VALIDATION_ERROR";
        String message = (field != null ? field + ": " : "") + defaultMsg;
        if ("password".equals(field)) {
            code = "WEAK_PASSWORD";
            message = "Password must be at least 8 characters.";
        } else if ("email".equals(field)) {
            code = "INVALID_EMAIL";
            message = "Please enter a valid email address.";
        }
        return ResponseEntity.badRequest().body(new ErrorResponse(code, message));
    }

    // ใช้กับ register ซ้ำอีเมล
    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyUsed(
            EmailAlreadyUsedException ex,
            HttpServletRequest request
    ) {
        ErrorResponse body = new ErrorResponse(
                "EMAIL_ALREADY_USED",
                "This email is already in use"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}

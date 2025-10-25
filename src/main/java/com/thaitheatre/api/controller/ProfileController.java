package com.thaitheatre.api.controller;

import com.thaitheatre.api.model.dto.ProfileRequest;
import com.thaitheatre.api.model.dto.ProfileResponse;
import com.thaitheatre.api.security.JwtUtil;
import com.thaitheatre.api.service.ProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth") // ✅ ให้ Swagger ใส่ Authorization header ให้อัตโนมัติ
public class ProfileController {

    private final ProfileService profileService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Create/Update profile (upsert)")
    @PostMapping(path = "/save", consumes = "application/json", produces = "application/json")
    public ProfileResponse saveProfile(
            @Valid @RequestBody ProfileRequest request,
            HttpServletRequest req
    ) {
        Long userId = requireUserId(req);
        return profileService.createOrUpdate(userId, request);
    }

    @Operation(summary = "Update profile (explicit PUT)")
    @PutMapping(path = "/me", consumes = "application/json", produces = "application/json")
    public ProfileResponse updateMyProfile(
            @Valid @RequestBody ProfileRequest request,
            HttpServletRequest req
    ) {
        Long userId = requireUserId(req);
        return profileService.createOrUpdate(userId, request);
    }

    @Operation(summary = "Get my profile")
    @GetMapping(path = "/me", produces = "application/json")
    public ProfileResponse getMyProfile(HttpServletRequest req) {
        Long userId = requireUserId(req);
        return profileService.getMy(userId);
    }

    // -------------------- helpers --------------------
    private Long requireUserId(HttpServletRequest req) {
        String authHeader = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || authHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization header");
        }

        String trimmed = authHeader.trim();
        String token = trimmed.regionMatches(true, 0, "Bearer ", 0, 7)
                ? trimmed.substring(7).trim()
                : trimmed; // เผื่อกรณีที่ client ส่งมาเป็น token ล้วนๆ

        if (token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Empty bearer token");
        }

        try {
            String sub = jwtUtil.validateAndGetSubject(token); // ควรเป็น userId ในรูปแบบ string
            return Long.valueOf(sub);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token subject is not a valid user id");
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
    }
}

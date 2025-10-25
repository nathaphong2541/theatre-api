package com.thaitheatre.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController; // ใช้หา userId จาก email ถ้า principal ไม่มี id
import org.springframework.web.server.ResponseStatusException;

import com.thaitheatre.api.model.dto.ProfileRequest;
import com.thaitheatre.api.model.dto.ProfileResponse;
import com.thaitheatre.api.repository.UserRepository;
import com.thaitheatre.api.service.ProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final ProfileService profileService;
    private final UserRepository userRepository; // สำหรับ map email -> userId เมื่อจำเป็น

    @Operation(summary = "Create/Update profile (upsert)")
    @PostMapping(path = "/save", consumes = "application/json", produces = "application/json")
    public ProfileResponse saveProfile(@Valid @RequestBody ProfileRequest request) {
        Long userId = requireUserIdFromSecurityContext();
        return profileService.createOrUpdate(userId, request);
    }

    @Operation(summary = "Update profile (explicit PUT)")
    @PutMapping(path = "/me", consumes = "application/json", produces = "application/json")
    public ProfileResponse updateMyProfile(@Valid @RequestBody ProfileRequest request) {
        Long userId = requireUserIdFromSecurityContext();
        return profileService.createOrUpdate(userId, request);
    }

    @Operation(summary = "Get my profile")
    @GetMapping(path = "/me", produces = "application/json")
    public ProfileResponse getMyProfile(@AuthenticationPrincipal User principal) {
        // ใช้ principal โดยตรง (Spring เติมให้จาก SecurityContext)
        // principal.getUsername() = email/username จาก JWT
        Long userId = resolveUserId(principal);
        return profileService.getMy(userId);
    }

    // -------------------- helpers --------------------
    /**
     * ดึง userId จาก SecurityContext (ไม่อ่าน Header เอง)
     */
    private Long requireUserIdFromSecurityContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        Object p = auth.getPrincipal();

        // กรณี principal เป็น org.springframework.security.core.userdetails.User
        String username = auth.getName(); // หรือ ((User)p).getUsername()
        return userRepository.findByEmail(username)
                .map(u -> u.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private Long resolveUserId(User principal) {
        // ถ้าคุณมี CustomUserDetails ให้เปลี่ยน method นี้ให้รองรับเช่นกัน
        return userRepository.findByEmail(principal.getUsername())
                .map(u -> u.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    // ถ้ายังไม่มี CustomUserDetails ให้ลบ if (p instanceof CustomUserDetails) ทิ้งได้
    // หรือสร้างคลาส:
    // public interface CustomUserDetails extends UserDetails { Long getId(); }
}

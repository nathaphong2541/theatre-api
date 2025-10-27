package com.thaitheatre.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
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
    private final UserRepository userRepository;

    // ===== JSON only (คงไว้ใช้ได้เหมือนเดิม) =====
    @Operation(summary = "Create/Update profile (JSON only)")
    @PostMapping(path = "/save", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProfileResponse saveProfile(@Valid @RequestBody ProfileRequest request) {
        Long userId = requireUserIdFromSecurityContext();
        return profileService.createOrUpdate(userId, request, null);
    }

    @Operation(summary = "Update my profile (JSON only, PUT)")
    @PutMapping(path = "/me", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProfileResponse updateMyProfile(@Valid @RequestBody ProfileRequest request) {
        Long userId = requireUserIdFromSecurityContext();
        return profileService.createOrUpdate(userId, request, null);
    }

    // ===== Multipart (JSON + avatar) =====
    @Operation(summary = "Create/Update profile with avatar (multipart/form-data)")
    @PostMapping(path = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProfileResponse saveProfileMultipart(
            @RequestPart("json") @Valid ProfileRequest request,
            @RequestPart(name = "avatar", required = false) MultipartFile avatar
    ) {
        Long userId = requireUserIdFromSecurityContext();
        // ต้องมีเมธอด overload ใน ProfileService: createOrUpdate(Long, ProfileRequest, MultipartFile)
        return profileService.createOrUpdate(userId, request, avatar);
    }

    @Operation(summary = "Update my profile with avatar (multipart/form-data, PUT)")
    @PutMapping(path = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProfileResponse updateMyProfileMultipart(
            @RequestPart("json") @Valid ProfileRequest request,
            @RequestPart(name = "avatar", required = false) MultipartFile avatar
    ) {
        Long userId = requireUserIdFromSecurityContext();
        return profileService.createOrUpdate(userId, request, avatar);
    }

    // ===== Avatar only =====
    @Operation(summary = "Upload/replace avatar only")
    @PutMapping(path = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProfileResponse uploadAvatarOnly(@RequestPart("avatar") MultipartFile avatar) {
        Long userId = requireUserIdFromSecurityContext();
        // ส่ง ProfileRequest เดิม (ไม่แก้อะไร) = null-safe ใน service
        return profileService.updateAvatarOnly(userId, avatar);
    }

    @Operation(summary = "Delete avatar")
    @DeleteMapping(path = "/avatar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProfileResponse deleteAvatar() {
        Long userId = requireUserIdFromSecurityContext();
        return profileService.deleteAvatar(userId);
    }

    // ===== Read my profile =====
    @Operation(summary = "Get my profile")
    @GetMapping(path = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProfileResponse getMyProfile(@AuthenticationPrincipal User principal) {
        Long userId = resolveUserId(principal);
        return profileService.getMy(userId);
    }

    // -------------------- helpers --------------------
    private Long requireUserIdFromSecurityContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        String username = auth.getName(); // อีเมล/ยูสเนมจาก Security
        return userRepository.findByEmail(username)
                .map(u -> u.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private Long resolveUserId(User principal) {
        return userRepository.findByEmail(principal.getUsername())
                .map(u -> u.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}

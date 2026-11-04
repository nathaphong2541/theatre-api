package com.thaitheatre.api.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thaitheatre.api.model.dto.admin.AdminUserDeleteRequest;
import com.thaitheatre.api.model.dto.admin.AdminUserDto;
import com.thaitheatre.api.model.dto.admin.AdminUserRoleRequest;
import com.thaitheatre.api.model.dto.admin.AdminUserStatusRequest;
import com.thaitheatre.api.security.HasUserId;
import com.thaitheatre.api.service.AdminUserService;

import lombok.RequiredArgsConstructor;

/**
 * Admin user management. Secured by the SecurityConfig rule requiring the ADMIN
 * role on /api/admin/**. List/search accounts, change role, enable/disable.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService service;

    @GetMapping
    public Page<AdminUserDto> list(@RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        return service.list(search, page, size, currentAdminId(auth));
    }

    /** Change a user's role (USER / ADMIN). */
    @PatchMapping("/{id}/role")
    public AdminUserDto changeRole(@PathVariable Long id, @RequestBody AdminUserRoleRequest req, Authentication auth) {
        return service.changeRole(id, req.getRole(), req.getReason(), currentAdminId(auth), adminEmail(auth));
    }

    /** Enable or disable a user's login. */
    @PatchMapping("/{id}/status")
    public AdminUserDto setStatus(@PathVariable Long id, @RequestBody AdminUserStatusRequest req, Authentication auth) {
        return service.setActive(id, req.isActive(), req.getReason(), currentAdminId(auth), adminEmail(auth));
    }

    /** Soft-delete a user account (reason recorded in the audit log). */
    @DeleteMapping("/{id}")
    public AdminUserDto delete(@PathVariable Long id,
            @RequestBody(required = false) AdminUserDeleteRequest req, Authentication auth) {
        String reason = req != null ? req.getReason() : null;
        return service.softDelete(id, reason, currentAdminId(auth), adminEmail(auth));
    }

    private Long currentAdminId(Authentication auth) {
        return (auth != null && auth.getPrincipal() instanceof HasUserId p) ? p.getId() : null;
    }

    private String adminEmail(Authentication auth) {
        return auth != null ? auth.getName() : null;
    }
}

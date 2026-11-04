package com.thaitheatre.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.thaitheatre.api.common.DelFlag;
import com.thaitheatre.api.common.RecordStatus;
import com.thaitheatre.api.model.dto.admin.AdminUserDto;
import com.thaitheatre.api.model.entity.UserAccount;
import com.thaitheatre.api.model.enums.UserRole;
import com.thaitheatre.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Admin user management: list/search accounts, change role (USER↔ADMIN),
 * enable/disable login, and soft-delete. Guarded by /api/admin/** (hasRole
 * ADMIN). Self-protection rules prevent an admin from locking themselves out
 * (can't demote, disable, or delete their own account). Every change is written
 * to the audit log with the acting admin and the reason they gave.
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    public static final String ENTITY_USER = "USER";
    public static final String ACTION_ROLE_CHANGED = "USER_ROLE_CHANGED";
    public static final String ACTION_ENABLED = "USER_ENABLED";
    public static final String ACTION_DISABLED = "USER_DISABLED";
    public static final String ACTION_DELETED = "USER_DELETED";

    private final UserRepository userRepo;
    private final AuditLogService auditLog;
    private final ScriptService scriptService;
    private final JobPostService jobPostService;
    private final ProfileService profileService;
    private final com.thaitheatre.api.repository.PasswordResetTokenRepository tokenRepo;

    @Transactional(readOnly = true)
    public Page<AdminUserDto> list(String search, int page, int size, Long currentAdminId) {
        var pageable = PageRequest.of(page, size);
        Page<UserAccount> result = (search == null || search.isBlank())
                ? userRepo.findAllByOrderByCreatedAtDesc(pageable)
                : userRepo
                        .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByCreatedAtDesc(
                                search.trim(), search.trim(), search.trim(), pageable);
        return result.map(u -> toDto(u, currentAdminId));
    }

    @Transactional
    public AdminUserDto changeRole(Long id, String roleRaw, String reason, Long adminId, String adminEmail) {
        UserAccount u = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserRole newRole;
        try {
            newRole = UserRole.valueOf(roleRaw == null ? "" : roleRaw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "role must be USER or ADMIN");
        }

        // Self-protection: an admin cannot demote their own account.
        if (u.getId().equals(adminId) && newRole != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot remove your own admin role");
        }

        UserRole oldRole = u.getRole();
        u.setRole(newRole);
        UserAccount saved = userRepo.save(u);

        audit(ACTION_ROLE_CHANGED, saved, adminId, adminEmail,
                "เปลี่ยนสิทธิ์ " + oldRole + " → " + newRole, reason);
        return toDto(saved, adminId);
    }

    @Transactional
    public AdminUserDto setActive(Long id, boolean active, String reason, Long adminId, String adminEmail) {
        UserAccount u = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Self-protection: an admin cannot disable their own account.
        if (u.getId().equals(adminId) && !active) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot disable your own account");
        }

        u.setRecordStatus(active ? RecordStatus.A : RecordStatus.I);
        UserAccount saved = userRepo.save(u);

        audit(active ? ACTION_ENABLED : ACTION_DISABLED, saved, adminId, adminEmail,
                active ? "เปิดใช้งานบัญชี" : "ปิดใช้งานบัญชี", reason);
        return toDto(saved, adminId);
    }

    /**
     * HARD delete: permanently remove the user and ALL their data (profile,
     * scripts + files, job posts + images, reset tokens, then the account row).
     * The email becomes free to register again. The audit row is written first
     * and intentionally survives (audit_logs has no FK to users).
     */
    @Transactional
    public AdminUserDto softDelete(Long id, String reason, Long adminId, String adminEmail) {
        UserAccount u = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Self-protection: an admin cannot delete their own account.
        if (u.getId().equals(adminId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot delete your own account");
        }

        // Snapshot for the response + record the action BEFORE the row is gone.
        AdminUserDto snapshot = toDto(u, adminId);
        audit(ACTION_DELETED, u, adminId, adminEmail, "ลบบัญชีผู้ใช้ถาวร (hard delete)", reason);

        // Remove dependents first to avoid FK violations and orphaned files.
        tokenRepo.deleteByUser(u);                 // the one real FK into users
        scriptService.deleteAllByOwner(id);        // scripts + script_images/pdfs + files
        jobPostService.deleteAllByOwner(id);       // job posts + promo images
        profileService.hardDeleteByUser(id);       // profile + performances + avatar/resume files

        userRepo.delete(u);                        // finally the account itself
        return snapshot;
    }

    /** Write an audit row describing an admin action on a user account. */
    private void audit(String action, UserAccount target, Long adminId, String adminEmail,
            String what, String reason) {
        String name = ((target.getFirstName() == null ? "" : target.getFirstName()) + " "
                + (target.getLastName() == null ? "" : target.getLastName())).trim();
        String title = name.isEmpty() ? target.getEmail() : name + " (" + target.getEmail() + ")";
        String detail = (reason == null || reason.isBlank()) ? what : what + " — เหตุผล: " + reason;
        auditLog.record(action, ENTITY_USER, target.getId(), title,
                adminId, adminEmail, UserRole.ADMIN.name(), target.getId(), detail);
    }

    private AdminUserDto toDto(UserAccount u, Long currentAdminId) {
        return AdminUserDto.builder()
                .id(u.getId())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .email(u.getEmail())
                .role(u.getRole() == null ? UserRole.USER.name() : u.getRole().name())
                .recordStatus(u.getRecordStatus() == null ? RecordStatus.A.name() : u.getRecordStatus().name())
                .delFlag(u.getDelFlag() == null ? DelFlag.N.name() : u.getDelFlag().name())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .self(u.getId().equals(currentAdminId))
                .build();
    }
}

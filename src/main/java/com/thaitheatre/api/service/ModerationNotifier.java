package com.thaitheatre.api.service;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.thaitheatre.api.common.DelFlag;
import com.thaitheatre.api.common.RecordStatus;
import com.thaitheatre.api.model.entity.UserAccount;
import com.thaitheatre.api.model.enums.UserRole;
import com.thaitheatre.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sends moderation emails and writes the audit trail for both job posts and
 * scripts. Runs asynchronously (mailExecutor) so slow SMTP never blocks the
 * user's submit/approve request, and swallows failures so a mail/log outage
 * cannot break moderation.
 *
 * Entity types: "JOB_POST", "SCRIPT". Item kind is the Thai label used in the
 * email subject/body ("ประกาศหาคนทำงาน" / "บทละคร").
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModerationNotifier {

    public static final String ENTITY_JOB_POST = "JOB_POST";
    public static final String ENTITY_SCRIPT = "SCRIPT";

    public static final String ACTION_SUBMITTED = "SUBMITTED";
    public static final String ACTION_RESUBMITTED = "RESUBMITTED";
    public static final String ACTION_APPROVED = "APPROVED";
    public static final String ACTION_REJECTED = "REJECTED";

    private final UserRepository userRepo;
    private final MailService mailService;
    private final AuditLogService auditLog;

    /**
     * Owner submitted (or re-submitted after an edit) an item that now awaits
     * review. Emails every admin and records the event.
     */
    @Async("mailExecutor")
    public void onSubmitted(String entityType, Long entityId, String title,
            Long ownerId, boolean isResubmit) {
        String itemKind = kind(entityType);
        String ownerName = displayName(ownerId);
        String ownerEmail = emailOf(ownerId);

        // Notify all active admins.
        for (UserAccount admin : admins()) {
            try {
                mailService.sendModerationSubmitted(admin.getEmail(), itemKind, title, ownerName);
            } catch (Exception e) {
                log.warn("submit notify failed to admin {}: {}", admin.getEmail(), e.getMessage());
            }
        }

        auditLog.record(
                (isResubmit ? ACTION_RESUBMITTED : ACTION_SUBMITTED),
                entityType, entityId, title,
                ownerId, ownerEmail, UserRole.USER.name(),
                ownerId,
                isResubmit ? "แก้ไขและส่งกลับเข้าตรวจสอบใหม่" : "ส่งคำขอตรวจสอบ");
    }

    /**
     * Admin approved an item. Emails the owner and records who approved it.
     */
    @Async("mailExecutor")
    public void onApproved(String entityType, Long entityId, String title,
            Long ownerId, Long adminId, String adminEmail) {
        String itemKind = kind(entityType);
        String ownerName = displayName(ownerId);
        String ownerEmail = emailOf(ownerId);

        if (ownerEmail != null) {
            try {
                mailService.sendModerationApproved(ownerEmail, ownerName, itemKind, title);
            } catch (Exception e) {
                log.warn("approve notify failed to owner {}: {}", ownerEmail, e.getMessage());
            }
        }

        auditLog.record(ACTION_APPROVED, entityType, entityId, title,
                adminId, adminEmail, UserRole.ADMIN.name(), ownerId, "อนุมัติ");
    }

    /**
     * Admin rejected an item. Emails the owner with the reason and records it.
     */
    @Async("mailExecutor")
    public void onRejected(String entityType, Long entityId, String title,
            Long ownerId, Long adminId, String adminEmail, String reason) {
        String itemKind = kind(entityType);
        String ownerName = displayName(ownerId);
        String ownerEmail = emailOf(ownerId);

        if (ownerEmail != null) {
            try {
                mailService.sendModerationRejected(ownerEmail, ownerName, itemKind, title, reason);
            } catch (Exception e) {
                log.warn("reject notify failed to owner {}: {}", ownerEmail, e.getMessage());
            }
        }

        auditLog.record(ACTION_REJECTED, entityType, entityId, title,
                adminId, adminEmail, UserRole.ADMIN.name(), ownerId,
                (reason == null || reason.isBlank()) ? "ปฏิเสธ (ไม่ระบุเหตุผล)" : "ปฏิเสธ: " + reason);
    }

    // ---------- helpers ----------

    private List<UserAccount> admins() {
        return userRepo.findByRoleAndDelFlagAndRecordStatus(UserRole.ADMIN, DelFlag.N, RecordStatus.A);
    }

    private String emailOf(Long userId) {
        if (userId == null) {
            return null;
        }
        return userRepo.findById(userId).map(UserAccount::getEmail).orElse(null);
    }

    private String displayName(Long userId) {
        if (userId == null) {
            return "";
        }
        return userRepo.findById(userId)
                .map(u -> ((u.getFirstName() == null ? "" : u.getFirstName()) + " "
                        + (u.getLastName() == null ? "" : u.getLastName())).trim())
                .filter(s -> !s.isEmpty())
                .orElse("");
    }

    private String kind(String entityType) {
        return ENTITY_SCRIPT.equals(entityType) ? "บทละคร" : "ประกาศหาคนทำงาน";
    }
}

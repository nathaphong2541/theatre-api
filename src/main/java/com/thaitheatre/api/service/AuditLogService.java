package com.thaitheatre.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.thaitheatre.api.model.entity.AuditLog;
import com.thaitheatre.api.repository.AuditLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Records and reads the moderation audit trail. Every submit / approve / reject
 * is logged here so a super admin can see the full history. Recording never
 * breaks the main flow: each write runs in its own transaction and swallows
 * failures (a logging outage must not block a user's submit or an approval).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository repo;

    /**
     * Insert one audit row in a brand-new transaction so it is not rolled back
     * with (or rolling back) the caller's transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String action, String entityType, Long entityId, String entityTitle,
            Long actorId, String actorEmail, String actorRole, Long ownerId, String detail) {
        try {
            AuditLog logEntry = new AuditLog();
            logEntry.setAction(action);
            logEntry.setEntityType(entityType);
            logEntry.setEntityId(entityId);
            logEntry.setEntityTitle(entityTitle);
            logEntry.setActorId(actorId);
            logEntry.setActorEmail(actorEmail);
            logEntry.setActorRole(actorRole);
            logEntry.setOwnerId(ownerId);
            logEntry.setDetail(detail);
            repo.save(logEntry);
        } catch (Exception e) {
            // Never let auditing break the primary operation.
            log.warn("Failed to write audit log action={} entity={}#{}: {}",
                    action, entityType, entityId, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> list(String entityType, int page, int size) {
        var pageable = PageRequest.of(page, size);
        return (entityType == null || entityType.isBlank())
                ? repo.findAllByOrderByCreatedAtDesc(pageable)
                : repo.findByEntityTypeOrderByCreatedAtDesc(entityType.trim().toUpperCase(), pageable);
    }
}

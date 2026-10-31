package com.thaitheatre.api.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thaitheatre.api.model.entity.AuditLog;
import com.thaitheatre.api.service.AuditLogService;

import lombok.RequiredArgsConstructor;

/**
 * Super-admin view of the moderation audit trail. Secured by the SecurityConfig
 * rule requiring the ADMIN role on /api/admin/**. Read-only.
 */
@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
public class AuditLogAdminController {

    private final AuditLogService service;

    /** Paged audit trail, optionally filtered by entity type (JOB_POST / SCRIPT). */
    @GetMapping
    public Page<AuditLog> list(@RequestParam(required = false) String entityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return service.list(entityType, page, size);
    }
}

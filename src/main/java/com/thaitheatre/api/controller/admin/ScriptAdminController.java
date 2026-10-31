package com.thaitheatre.api.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thaitheatre.api.model.dto.script.ScriptResponse;
import com.thaitheatre.api.model.dto.script.ScriptReviewRequest;
import com.thaitheatre.api.security.HasUserId;
import com.thaitheatre.api.service.ScriptService;

import lombok.RequiredArgsConstructor;

/**
 * Admin moderation of scripts (บทละคร / งานวิจัย). Secured by a SecurityConfig
 * rule requiring the ADMIN role on /api/admin/** (so only admins can list and
 * approve/reject). Only approved scripts appear in the public Play Library.
 */
@RestController
@RequestMapping("/api/admin/scripts")
@RequiredArgsConstructor
public class ScriptAdminController {

    private final ScriptService service;

    /** List scripts for moderation, optionally filtered by status (e.g. PENDING). */
    @GetMapping
    public Page<ScriptResponse> list(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.listForAdmin(status, page, size);
    }

    /** Approve or reject a script. The acting admin is captured for the audit log. */
    @PostMapping("/{id}/review")
    public ScriptResponse review(@PathVariable Long id, @RequestBody ScriptReviewRequest req,
            Authentication auth) {
        Long adminId = (auth != null && auth.getPrincipal() instanceof HasUserId p) ? p.getId() : null;
        String adminEmail = auth != null ? auth.getName() : null;
        return service.review(id, req, adminId, adminEmail);
    }
}

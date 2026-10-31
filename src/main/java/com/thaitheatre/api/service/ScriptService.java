package com.thaitheatre.api.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.thaitheatre.api.model.dto.script.ScriptCreateRequest;
import com.thaitheatre.api.model.dto.script.ScriptResponse;
import com.thaitheatre.api.model.dto.script.ScriptReviewRequest;
import com.thaitheatre.api.model.dto.script.ScriptUpdateRequest;

public interface ScriptService {

        ScriptResponse createScript(
                        ScriptCreateRequest request,
                        List<MultipartFile> images,
                        MultipartFile pdfFile,
                        Long currentUserId);

        ScriptResponse addPdfVersion(
                        Long scriptId,
                        MultipartFile pdfFile,
                        String versionName,
                        Long currentUserId);

        ScriptResponse updateScript(
                        Long id,
                        ScriptUpdateRequest request,
                        List<MultipartFile> images,
                        MultipartFile pdfFile,
                        Long currentUserId);

        ScriptResponse getScript(Long id);

        List<ScriptResponse> getAllScripts();

        /** Public Play Library: only admin-approved scripts. */
        List<ScriptResponse> getPublicScripts();

        /** Public detail: a single script, only if it is approved. */
        ScriptResponse getPublicScript(Long id);

        void deleteScript(Long id, Long currentUserId);

        List<ScriptResponse> getMyScripts(Long userId);

        ScriptResponse getMyScript(Long scriptId, Long userId);

        // ----- admin moderation -----

        /** Admin review queue, optionally filtered by status. */
        Page<ScriptResponse> listForAdmin(String status, int page, int size);

        /** Admin approves or rejects a script. The acting admin is captured for the audit log. */
        ScriptResponse review(Long id, ScriptReviewRequest req, Long adminId, String adminEmail);
}

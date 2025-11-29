package com.thaitheatre.api.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.thaitheatre.api.model.dto.script.ScriptCreateRequest;
import com.thaitheatre.api.model.dto.script.ScriptResponse;
import com.thaitheatre.api.model.dto.script.ScriptUpdateRequest;

public interface ScriptService {

    ScriptResponse createScript(ScriptCreateRequest request,
            List<MultipartFile> images,
            MultipartFile pdfFile,
            Long currentUserId);

    ScriptResponse addPdfVersion(Long scriptId,
            MultipartFile pdfFile,
            String versionName,
            Long currentUserId);

    ScriptResponse updateScript(Long id,
            ScriptUpdateRequest request,
            List<MultipartFile> images,
            MultipartFile pdfFile, // optional: ถ้ามีให้เพิ่มเวอร์ชันใหม่
            Long currentUserId);

    ScriptResponse getScript(Long id);

    List<ScriptResponse> getAllScripts();

    void deleteScript(Long id);
}

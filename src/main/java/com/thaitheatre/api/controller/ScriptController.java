package com.thaitheatre.api.controller;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.thaitheatre.api.model.dto.script.ScriptCreateRequest;
import com.thaitheatre.api.model.dto.script.ScriptResponse;
import com.thaitheatre.api.model.dto.script.ScriptUpdateRequest;
import com.thaitheatre.api.model.entity.Script;
import com.thaitheatre.api.model.entity.ScriptPdf;
import com.thaitheatre.api.repository.ScriptPdfRepository;
import com.thaitheatre.api.repository.ScriptRepository;
import com.thaitheatre.api.service.ScriptService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/scripts")
@RequiredArgsConstructor
public class ScriptController {

    private final ScriptService scriptService;

    private final ScriptRepository scriptRepository;
    private final ScriptPdfRepository scriptPdfRepository;

    // CREATE
    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<ScriptResponse> createScript(
            @RequestPart("data") ScriptCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart("pdf") MultipartFile pdfFile) {
        Long userId = getCurrentUserId();
        ScriptResponse response = scriptService.createScript(request, images, pdfFile, userId);
        return ResponseEntity.ok(response);
    }

    // UPDATE
    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    public ResponseEntity<ScriptResponse> updateScript(
            @PathVariable Long id,
            @RequestPart("data") ScriptUpdateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "pdf", required = false) MultipartFile pdfFile) {
        Long userId = getCurrentUserId();
        ScriptResponse response = scriptService.updateScript(id, request, images, pdfFile, userId);
        return ResponseEntity.ok(response);
    }

    // READ - get one
    @GetMapping("/{id}")
    public ResponseEntity<ScriptResponse> getScript(@PathVariable Long id) {
        return ResponseEntity.ok(scriptService.getScript(id));
    }

    // READ - list
    @GetMapping
    public ResponseEntity<List<ScriptResponse>> getAllScripts() {
        return ResponseEntity.ok(scriptService.getAllScripts());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScript(@PathVariable Long id) {
        scriptService.deleteScript(id);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthorized");
        }

        Object principal = auth.getPrincipal();

        // ถ้าคุณทำ CustomUserDetails ให้มี getId()
        if (principal instanceof com.thaitheatre.api.security.HasUserId p) {
            return p.getId();
        }

        throw new RuntimeException("Cannot read userId from principal. Use CustomUserDetails.");
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<Resource> downloadLatestPdf(@PathVariable Long id) throws MalformedURLException {
        Script script = scriptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Script not found"));

        ScriptPdf latest = script.getPdfs().stream()
                .max(Comparator.comparing(ScriptPdf::getVersionNo))
                .orElseThrow(() -> new RuntimeException("PDF not found for this script"));

        Path path = Path.of(latest.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists()) {
            throw new RuntimeException("PDF file not found on server");
        }

        String fileName = path.getFileName().toString();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @GetMapping("/{scriptId}/pdf/{pdfId}")
    public ResponseEntity<Resource> downloadPdfById(@PathVariable Long scriptId,
            @PathVariable Long pdfId) throws MalformedURLException {

        ScriptPdf pdf = scriptPdfRepository.findById(pdfId)
                .orElseThrow(() -> new RuntimeException("PDF not found"));

        // ป้องกันกรณีเรียก pdfId ไม่ตรง scriptId
        if (!pdf.getScript().getId().equals(scriptId)) {
            throw new RuntimeException("PDF does not belong to this script");
        }

        Path path = Path.of(pdf.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists()) {
            throw new RuntimeException("PDF file not found on server");
        }

        String fileName = path.getFileName().toString();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @GetMapping("/me")
    public ResponseEntity<List<ScriptResponse>> getMyScripts() {
        Long userId = getCurrentUserId(); // จาก JWT/Cookie
        return ResponseEntity.ok(scriptService.getMyScripts(userId));
    }

    @GetMapping("/me/{id}")
    public ResponseEntity<ScriptResponse> getMyScript(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(scriptService.getMyScript(id, userId));
    }

    @GetMapping("/me/{id}/pdf")
    public ResponseEntity<Resource> downloadMyLatestPdf(@PathVariable Long id) throws MalformedURLException {
        Long userId = getCurrentUserId();
        Script script = scriptRepository.findByIdAndCreatedBy(id, userId)
                .orElseThrow(() -> new RuntimeException("Script not found or no permission"));

        ScriptPdf latest = script.getPdfs().stream()
                .max(Comparator.comparing(ScriptPdf::getVersionNo))
                .orElseThrow(() -> new RuntimeException("PDF not found for this script"));

        Path path = Path.of(latest.getFilePath());
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists())
            throw new RuntimeException("PDF file not found on server");

        String fileName = path.getFileName().toString();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }
}

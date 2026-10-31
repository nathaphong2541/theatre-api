package com.thaitheatre.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.thaitheatre.api.model.dto.jobpost.JobPostCreateRequest;
import com.thaitheatre.api.model.dto.jobpost.JobPostResponse;
import com.thaitheatre.api.security.HasUserId;
import com.thaitheatre.api.service.JobPostService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Organization-owned job posts (create/manage your own). Requires login. */
@RestController
@RequestMapping("/api/job-posts")
@RequiredArgsConstructor
public class JobPostController {

    private final JobPostService service;

    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<JobPostResponse> create(
            @Valid @RequestPart("data") JobPostCreateRequest data,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(service.create(data, image, currentUserId()));
    }

    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    public ResponseEntity<JobPostResponse> update(
            @PathVariable Long id,
            @Valid @RequestPart("data") JobPostCreateRequest data,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(service.update(id, data, image, currentUserId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    /** Owner on/off switch: PATCH /{id}/enabled?value=true|false */
    @PatchMapping("/{id}/enabled")
    public ResponseEntity<JobPostResponse> setEnabled(@PathVariable Long id, @RequestParam boolean value) {
        return ResponseEntity.ok(service.setEnabled(id, value, currentUserId()));
    }

    @GetMapping("/me")
    public ResponseEntity<List<JobPostResponse>> listMine() {
        return ResponseEntity.ok(service.listMine(currentUserId()));
    }

    @GetMapping("/me/{id}")
    public ResponseEntity<JobPostResponse> getMine(@PathVariable Long id) {
        return ResponseEntity.ok(service.getMine(id, currentUserId()));
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthorized");
        }
        if (auth.getPrincipal() instanceof HasUserId p) {
            return p.getId();
        }
        throw new RuntimeException("Cannot read userId from principal.");
    }
}

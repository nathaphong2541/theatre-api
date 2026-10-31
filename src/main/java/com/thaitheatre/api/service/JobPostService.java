package com.thaitheatre.api.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.thaitheatre.api.model.dto.jobpost.JobPostCreateRequest;
import com.thaitheatre.api.model.dto.jobpost.JobPostResponse;
import com.thaitheatre.api.model.dto.jobpost.JobPostReviewRequest;
import com.thaitheatre.api.model.entity.JobPost;
import com.thaitheatre.api.repository.JobPostRepository;
import com.thaitheatre.api.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;

/**
 * Job-post ("หาคนทำงาน") business logic: organizations create/manage posts,
 * admins approve/reject, and the public sees only approved+enabled posts.
 */
@Service
@RequiredArgsConstructor
public class JobPostService {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    private final JobPostRepository repo;
    private final ProfileRepository profileRepo;
    private final FileStorageService fileStorage;
    private final ModerationNotifier notifier;

    @Value("${app.files.public-base-url}")
    private String publicBaseUrl; // e.g. /files/profile

    // ------------------------- owner -------------------------

    @Transactional
    public JobPostResponse create(JobPostCreateRequest req, MultipartFile image, Long userId) {
        JobPost p = new JobPost();
        p.setCreatedBy(userId);
        apply(p, req);
        // New posts always start awaiting admin review and enabled.
        p.setStatus(STATUS_PENDING);
        p.setEnabled(true);
        if (image != null && !image.isEmpty()) {
            p.setImagePath(saveImage(image, null));
        }
        JobPost saved = repo.save(p);
        notifier.onSubmitted(ModerationNotifier.ENTITY_JOB_POST, saved.getId(), saved.getTitle(),
                saved.getCreatedBy(), false);
        return toResponse(saved);
    }

    @Transactional
    public JobPostResponse update(Long id, JobPostCreateRequest req, MultipartFile image, Long userId) {
        JobPost p = repo.findByIdAndCreatedBy(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job post not found"));

        apply(p, req);
        // Any edit sends the post back to review (content changed).
        p.setStatus(STATUS_PENDING);
        p.setReviewNote(null);
        if (image != null && !image.isEmpty()) {
            p.setImagePath(saveImage(image, p.getImagePath()));
        }
        JobPost saved = repo.save(p);
        notifier.onSubmitted(ModerationNotifier.ENTITY_JOB_POST, saved.getId(), saved.getTitle(),
                saved.getCreatedBy(), true);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        JobPost p = repo.findByIdAndCreatedBy(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job post not found"));
        repo.delete(p);
    }

    /** Owner toggles the post on/off (does not change approval status). */
    @Transactional
    public JobPostResponse setEnabled(Long id, boolean enabled, Long userId) {
        JobPost p = repo.findByIdAndCreatedBy(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job post not found"));
        p.setEnabled(enabled);
        return toResponse(repo.save(p));
    }

    @Transactional(readOnly = true)
    public List<JobPostResponse> listMine(Long userId) {
        return repo.findByCreatedByOrderByCreatedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public JobPostResponse getMine(Long id, Long userId) {
        return toResponse(repo.findByIdAndCreatedBy(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job post not found")));
    }

    // ------------------------- public -------------------------

    @Transactional(readOnly = true)
    public Page<JobPostResponse> listPublic(int page, int size) {
        return repo.findPublic(java.time.LocalDate.now(), PageRequest.of(page, size)).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public JobPostResponse getPublic(Long id) {
        JobPost p = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job post not found"));
        var today = java.time.LocalDate.now();
        boolean inWindow = (p.getStartDate() == null || !p.getStartDate().isAfter(today))
                && (p.getEndDate() == null || !p.getEndDate().isBefore(today));
        if (!STATUS_APPROVED.equals(p.getStatus()) || !p.isEnabled() || !inWindow) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job post not found");
        }
        return toResponse(p);
    }

    // ------------------------- admin -------------------------

    @Transactional(readOnly = true)
    public Page<JobPostResponse> listForAdmin(String status, int page, int size) {
        var pageable = PageRequest.of(page, size);
        var result = (status == null || status.isBlank())
                ? repo.findAllByOrderByCreatedAtDesc(pageable)
                : repo.findByStatusOrderByCreatedAtDesc(status.trim().toUpperCase(), pageable);
        return result.map(this::toResponse);
    }

    /** Admin approves or rejects a post. */
    @Transactional
    public JobPostResponse review(Long id, JobPostReviewRequest req, Long adminId, String adminEmail) {
        JobPost p = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job post not found"));
        String status = req.getStatus() == null ? "" : req.getStatus().trim().toUpperCase();
        if (!STATUS_APPROVED.equals(status) && !STATUS_REJECTED.equals(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status must be APPROVED or REJECTED");
        }
        p.setStatus(status);
        p.setReviewNote(req.getReviewNote());
        JobPost saved = repo.save(p);

        if (STATUS_APPROVED.equals(status)) {
            notifier.onApproved(ModerationNotifier.ENTITY_JOB_POST, saved.getId(), saved.getTitle(),
                    saved.getCreatedBy(), adminId, adminEmail);
        } else {
            notifier.onRejected(ModerationNotifier.ENTITY_JOB_POST, saved.getId(), saved.getTitle(),
                    saved.getCreatedBy(), adminId, adminEmail, saved.getReviewNote());
        }
        return toResponse(saved);
    }

    // ------------------------- helpers -------------------------

    private void apply(JobPost p, JobPostCreateRequest req) {
        p.setTitle(req.getTitle());
        p.setDescription(req.getDescription());
        p.setProfessions(nvl(req.getProfessions()));
        p.setWorkLocations(nvl(req.getWorkLocations()));
        p.setExperience(nvl(req.getExperience()));
        p.setStartDate(req.getStartDate());
        p.setEndDate(req.getEndDate());
        if (p.getStartDate() != null && p.getEndDate() != null
                && p.getEndDate().isBefore(p.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endDate must not be before startDate");
        }
    }

    private String saveImage(MultipartFile image, String oldFilename) {
        try {
            return fileStorage.saveJobPostImage(image, oldFilename);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save image");
        }
    }

    private static List<Integer> nvl(List<Integer> v) {
        return v == null ? List.of() : v;
    }

    private JobPostResponse toResponse(JobPost p) {
        String imageUrl = null;
        if (p.getImagePath() != null && !p.getImagePath().isBlank()) {
            String base = publicBaseUrl.endsWith("/") ? publicBaseUrl : publicBaseUrl + "/";
            imageUrl = base + "jobpost/" + p.getImagePath();
        }
        String name = profileRepo.findByUserId(p.getCreatedBy())
                .map(pr -> ((pr.getFirstName() == null ? "" : pr.getFirstName()) + " "
                        + (pr.getLastName() == null ? "" : pr.getLastName())).trim())
                .filter(s -> !s.isEmpty())
                .orElse(null);

        return JobPostResponse.builder()
                .id(p.getId())
                .createdBy(p.getCreatedBy())
                .createdByName(name)
                .title(p.getTitle())
                .description(p.getDescription())
                .imageUrl(imageUrl)
                .professions(nvl(p.getProfessions()))
                .workLocations(nvl(p.getWorkLocations()))
                .experience(nvl(p.getExperience()))
                .status(p.getStatus())
                .enabled(p.isEnabled())
                .reviewNote(p.getReviewNote())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .expired(p.getEndDate() != null && p.getEndDate().isBefore(java.time.LocalDate.now()))
                .scheduled(p.getStartDate() != null && p.getStartDate().isAfter(java.time.LocalDate.now()))
                .build();
    }
}

package com.thaitheatre.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.JobPost;

public interface JobPostRepository extends JpaRepository<JobPost, Long> {

    /** Posts owned by an organization (newest first). */
    List<JobPost> findByCreatedByOrderByCreatedAtDesc(Long createdBy);

    /** A specific post owned by the caller (ownership-scoped fetch). */
    Optional<JobPost> findByIdAndCreatedBy(Long id, Long createdBy);

    /**
     * Public listing: approved + enabled, and within the scheduled window.
     * A null start date means "live now"; a null end date means "no expiry".
     * `today` is passed in so the comparison uses the server's current date.
     */
    @org.springframework.data.jpa.repository.Query("""
            SELECT j FROM JobPost j
            WHERE j.status = 'APPROVED' AND j.enabled = true
              AND (j.startDate IS NULL OR j.startDate <= :today)
              AND (j.endDate IS NULL OR j.endDate >= :today)
            ORDER BY j.createdAt DESC
            """)
    Page<JobPost> findPublic(@org.springframework.data.repository.query.Param("today") java.time.LocalDate today,
            Pageable pageable);

    /** Admin listing filtered by status (e.g. PENDING for the review queue). */
    Page<JobPost> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    /** Admin listing of everything. */
    Page<JobPost> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

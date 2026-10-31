package com.thaitheatre.api.model.entity;

import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * A recruitment post ("หาคนทำงาน") created by an organization-type profile.
 *
 * Workflow: a new post starts as PENDING and is hidden from the public until an
 * admin APPROVES it. The owner can also enable/disable it, and an admin can
 * reject it. Public listing shows only status=APPROVED AND enabled=true.
 */
@Entity
@Table(name = "job_posts")
@Getter
@Setter
public class JobPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owner = users.id (the organization profile that created this post). */
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    /** Promo image stored on disk; served via the files host. */
    @Column(name = "image_path", length = 500)
    private String imagePath;

    /** Profession/role ids being recruited (master: professions). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "professions", columnDefinition = "jsonb not null default '[]'::jsonb")
    private List<Integer> professions;

    /** Work-location ids (master: work-locations). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "work_locations", columnDefinition = "jsonb not null default '[]'::jsonb")
    private List<Integer> workLocations;

    /** Experience-level ids (master: experience-levels). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "experience", columnDefinition = "jsonb not null default '[]'::jsonb")
    private List<Integer> experience;

    /** PENDING (awaiting review) / APPROVED / REJECTED. */
    @Column(nullable = false, length = 20)
    private String status;

    /** When the post should start appearing publicly (null = immediately). */
    @Column(name = "start_date")
    private java.time.LocalDate startDate;

    /** Last day the post is public; after this it is treated as expired (null = never). */
    @Column(name = "end_date")
    private java.time.LocalDate endDate;

    /** Owner's on/off switch; a disabled post is hidden even when approved. */
    @Column(nullable = false)
    private boolean enabled;

    /** Admin note when rejecting (shown to the owner). */
    @Column(name = "review_note", length = 500)
    private String reviewNote;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (professions == null) {
            professions = List.of();
        }
        if (workLocations == null) {
            workLocations = List.of();
        }
        if (experience == null) {
            experience = List.of();
        }
        if (status == null) {
            status = "PENDING";
        }
    }
}

package com.thaitheatre.api.model.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Immutable audit trail of moderation activity (job posts & scripts) so a
 * super admin can review the full history: who submitted, who approved/rejected,
 * when, and why. Rows are only ever inserted — never updated or deleted.
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** What happened, e.g. JOB_POST_SUBMITTED, SCRIPT_APPROVED, SCRIPT_REJECTED. */
    @Column(nullable = false, length = 50)
    private String action;

    /** Target entity type: JOB_POST or SCRIPT. */
    @Column(name = "entity_type", nullable = false, length = 20)
    private String entityType;

    /** Id of the affected job post / script. */
    @Column(name = "entity_id")
    private Long entityId;

    /** Human-readable title of the affected item (snapshot at the time). */
    @Column(name = "entity_title", length = 255)
    private String entityTitle;

    /** users.id of the person who performed the action (owner or admin). */
    @Column(name = "actor_id")
    private Long actorId;

    /** Email of the actor (snapshot, so the log stays meaningful over time). */
    @Column(name = "actor_email", length = 150)
    private String actorEmail;

    /** Role of the actor at the time: USER (owner) or ADMIN. */
    @Column(name = "actor_role", length = 20)
    private String actorRole;

    /** users.id of the item owner (useful when an admin acts on someone's item). */
    @Column(name = "owner_id")
    private Long ownerId;

    /** Free-text detail, e.g. the reject reason shown to the owner. */
    @Column(name = "detail", length = 1000)
    private String detail;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}

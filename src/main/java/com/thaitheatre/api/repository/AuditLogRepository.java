package com.thaitheatre.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /** Full audit trail, newest first. */
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** Audit trail filtered by entity type (JOB_POST / SCRIPT), newest first. */
    Page<AuditLog> findByEntityTypeOrderByCreatedAtDesc(String entityType, Pageable pageable);
}

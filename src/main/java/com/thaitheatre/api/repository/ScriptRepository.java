package com.thaitheatre.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.Script;

public interface ScriptRepository extends JpaRepository<Script, Long> {

    List<Script> findAllByCreatedBy(Long createdBy);

    Optional<Script> findByIdAndCreatedBy(Long id, Long createdBy);

    /** Public Play Library: only admin-approved scripts (newest first). */
    List<Script> findByStatusOrderByCreatedAtDesc(String status);

    /** Admin review queue filtered by status (e.g. PENDING). */
    Page<Script> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    /** Admin listing of everything. */
    Page<Script> findAllByOrderByCreatedAtDesc(Pageable pageable);

}

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

    /**
     * Public Play Library with an owner-active guard: approved scripts whose
     * creator is still an active (not deleted/disabled) user. Hides content of
     * soft-deleted accounts.
     */
    @org.springframework.data.jpa.repository.Query("""
            SELECT s FROM Script s
            WHERE s.status = :status
              AND EXISTS (SELECT 1 FROM UserAccount u
                          WHERE u.id = s.createdBy
                            AND u.delFlag = com.thaitheatre.api.common.DelFlag.N
                            AND u.recordStatus = com.thaitheatre.api.common.RecordStatus.A)
            ORDER BY s.createdAt DESC
            """)
    List<Script> findPublicByStatus(@org.springframework.data.repository.query.Param("status") String status);

    /** Admin review queue filtered by status (e.g. PENDING). */
    Page<Script> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    /** Admin listing of everything. */
    Page<Script> findAllByOrderByCreatedAtDesc(Pageable pageable);

}

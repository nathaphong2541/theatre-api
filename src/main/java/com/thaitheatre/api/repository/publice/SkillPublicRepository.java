package com.thaitheatre.api.repository.publice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.admin.Skill;

public interface SkillPublicRepository extends JpaRepository<Skill, Long> {
    Page<Skill> findByDelFlagAndRecordStatus(String delFlag, String recordStatus, Pageable pageable);

    Page<Skill> findByDelFlagAndRecordStatusAndPosition_Id(String delFlag, String recordStatus, Long positionId,
            Pageable pageable);
}

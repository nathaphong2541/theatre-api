package com.thaitheatre.api.repository.publice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.admin.SpecialSkill;

public interface SpecialSkillPublicRepository extends JpaRepository<SpecialSkill, Long> {
    Page<SpecialSkill> findByDelFlagAndRecordStatus(String delFlag, String recordStatus, Pageable pageable);
}

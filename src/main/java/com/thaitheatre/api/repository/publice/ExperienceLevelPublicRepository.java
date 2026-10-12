package com.thaitheatre.api.repository.publice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.admin.ExperienceLevel;

public interface ExperienceLevelPublicRepository extends JpaRepository<ExperienceLevel, Long> {
    Page<ExperienceLevel> findByDelFlagAndRecordStatus(String delFlag, String recordStatus, Pageable pageable);
}

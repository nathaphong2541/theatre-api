package com.thaitheatre.api.repository.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.admin.ExperienceLevel;

public interface ExperienceLevelRepository extends JpaRepository<ExperienceLevel, Long> {
}
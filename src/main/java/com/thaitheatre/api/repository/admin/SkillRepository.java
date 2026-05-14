package com.thaitheatre.api.repository.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.admin.Skill;

public interface SkillRepository extends JpaRepository<Skill, Long> {
}

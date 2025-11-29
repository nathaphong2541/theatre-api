package com.thaitheatre.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.ScriptImage;

public interface ScriptImageRepository extends JpaRepository<ScriptImage, Long> {
}

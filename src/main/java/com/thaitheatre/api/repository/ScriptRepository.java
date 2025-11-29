package com.thaitheatre.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.Script;

public interface ScriptRepository extends JpaRepository<Script, Long> {
}

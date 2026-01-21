package com.thaitheatre.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.Script;

public interface ScriptRepository extends JpaRepository<Script, Long> {

    List<Script> findAllByCreatedBy(Long createdBy);

    Optional<Script> findByIdAndCreatedBy(Long id, Long createdBy);

}

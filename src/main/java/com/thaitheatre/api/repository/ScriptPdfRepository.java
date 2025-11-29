package com.thaitheatre.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.ScriptPdf;

public interface ScriptPdfRepository extends JpaRepository<ScriptPdf, Long> {

    List<ScriptPdf> findByScriptIdOrderByVersionNoAsc(Long scriptId);

    Optional<ScriptPdf> findTopByScriptIdOrderByVersionNoDesc(Long scriptId);
}

package com.thaitheatre.api.repository.publice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.admin.Profession;

public interface ProfessionPublicRepository extends JpaRepository<Profession, Long> {
    Page<Profession> findByDelFlagAndRecordStatus(String delFlag, String recordStatus, Pageable pageable);
}

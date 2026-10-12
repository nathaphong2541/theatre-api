package com.thaitheatre.api.repository.publice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.admin.WorkLocation;

public interface WorkLocationPublicRepository extends JpaRepository<WorkLocation, Long> {
    Page<WorkLocation> findByDelFlagAndRecordStatus(String delFlag, String recordStatus, Pageable pageable);
}

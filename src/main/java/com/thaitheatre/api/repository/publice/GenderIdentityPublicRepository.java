package com.thaitheatre.api.repository.publice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.admin.GenderIdentity;

public interface GenderIdentityPublicRepository extends JpaRepository<GenderIdentity, Long> {
    Page<GenderIdentity> findByDelFlagAndRecordStatus(String delFlag, String recordStatus, Pageable pageable);
}

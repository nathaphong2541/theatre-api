package com.thaitheatre.api.repository.publice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.admin.PersonalIdentity;

public interface PersonalIdentityPublicRepository extends JpaRepository<PersonalIdentity, Long> {
    Page<PersonalIdentity> findByDelFlagAndRecordStatus(String delFlag, String recordStatus, Pageable pageable);
}

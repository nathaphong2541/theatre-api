package com.thaitheatre.api.repository.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.admin.GenderIdentity;

public interface GenderIdentityRepository extends JpaRepository<GenderIdentity, Long> {
}

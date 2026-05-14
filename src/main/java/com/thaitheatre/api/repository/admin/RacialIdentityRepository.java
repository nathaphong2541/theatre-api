package com.thaitheatre.api.repository.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.admin.RacialIdentity;

public interface RacialIdentityRepository extends JpaRepository<RacialIdentity, Long> {
}

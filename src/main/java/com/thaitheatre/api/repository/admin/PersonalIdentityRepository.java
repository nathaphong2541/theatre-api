package com.thaitheatre.api.repository.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.admin.PersonalIdentity;

public interface PersonalIdentityRepository extends JpaRepository<PersonalIdentity, Long> {
}

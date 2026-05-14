package com.thaitheatre.api.repository.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.admin.Profession;

public interface ProfessionRepository extends JpaRepository<Profession, Long> {
}

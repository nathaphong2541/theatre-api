package com.thaitheatre.api.repository.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.admin.WorkLocation;

public interface WorkLocationRepository extends JpaRepository<WorkLocation, Long> {
}

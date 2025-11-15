package com.thaitheatre.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.ProfilePerformance;

public interface ProfilePerformanceRepository
        extends JpaRepository<ProfilePerformance, Long> {

    List<ProfilePerformance> findByProfileIdOrderBySortOrderAscCreatedAtAsc(Long profileId);

    long countByProfileId(Long profileId);
}
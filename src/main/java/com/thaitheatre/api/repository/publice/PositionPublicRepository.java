package com.thaitheatre.api.repository.publice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.admin.Position;

public interface PositionPublicRepository extends JpaRepository<Position, Long> {
    Page<Position> findByDelFlagAndRecordStatus(String delFlag, String recordStatus, Pageable pageable);

    Page<Position> findByDelFlagAndRecordStatusAndDepartment_Id(String delFlag, String recordStatus, Long departmentId,
            Pageable pageable);

    Page<Position> findByDepartment_Id(Long departmentId, Pageable pageable);
}

package com.thaitheatre.api.repository.publice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.admin.Department;

public interface DepartmentPublicRepository extends JpaRepository<Department, Long> {
    Page<Department> findByDelFlagAndRecordStatus(String delFlag, String recordStatus, Pageable pageable);
}

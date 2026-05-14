package com.thaitheatre.api.repository.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.admin.Department;


public interface DepartmentRepository extends JpaRepository<Department, Long> {
}

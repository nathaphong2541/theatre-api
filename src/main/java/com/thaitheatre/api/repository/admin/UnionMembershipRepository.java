package com.thaitheatre.api.repository.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.admin.UnionMembership;

public interface UnionMembershipRepository extends JpaRepository<UnionMembership, Long> {
}

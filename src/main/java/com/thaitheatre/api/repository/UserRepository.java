package com.thaitheatre.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.common.DelFlag;
import com.thaitheatre.api.common.RecordStatus;
import com.thaitheatre.api.model.entity.UserAccount;
import com.thaitheatre.api.model.enums.UserRole;

public interface UserRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Active, non-deleted accounts with a given role (e.g. all admins). */
    List<UserAccount> findByRoleAndDelFlagAndRecordStatus(UserRole role, DelFlag delFlag, RecordStatus recordStatus);
}

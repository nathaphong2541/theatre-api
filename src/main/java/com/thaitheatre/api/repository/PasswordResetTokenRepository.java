package com.thaitheatre.api.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.PasswordResetToken;
import com.thaitheatre.api.model.entity.UserAccount;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByUserAndUsedFalseAndExpiresAtAfter(
            UserAccount user,
            Instant now
    );

    // ✅ ใหม่: หาโดย tokenHash + not used + ยังไม่หมดอายุ
    Optional<PasswordResetToken> findByTokenHashAndUsedFalseAndExpiresAtAfter(
            String tokenHash,
            Instant now
    );

    // mark all of a user's still-valid tokens as used (invalidate old links on new request)
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(
            "update PasswordResetToken t set t.used = true where t.user = :user and t.used = false")
    int markAllUsedForUser(com.thaitheatre.api.model.entity.UserAccount user);
}

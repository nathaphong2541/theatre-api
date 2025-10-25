package com.thaitheatre.api.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thaitheatre.api.model.entity.PasswordResetToken;
import com.thaitheatre.api.model.entity.UserAccount;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findFirstByUserAndUsedFalseOrderByCreatedAtDesc(UserAccount user);

    Optional<PasswordResetToken> findByUserAndUsedFalseAndExpiresAtAfter(UserAccount user, Instant now);
    // we will locate by user and check token with hash
}

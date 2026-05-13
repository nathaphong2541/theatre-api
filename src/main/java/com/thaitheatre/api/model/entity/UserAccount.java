package com.thaitheatre.api.model.entity;

import java.time.Instant;

import com.thaitheatre.api.common.DelFlag;
import com.thaitheatre.api.common.RecordStatus;
import com.thaitheatre.api.model.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Basic info ---
    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 150)
    private String email; // ใช้เป็น username

    @Column(nullable = false, length = 255)
    private String passwordHash; // BCrypt hash

    @Column(nullable = false)
    private Boolean policyConfirmed = false;

    // --- Role ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    // --- Status flags ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private RecordStatus recordStatus = RecordStatus.A; // A=Active, I=Inactive

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private DelFlag delFlag = DelFlag.N; // N=Not deleted, Y=Deleted

    // --- Audit ---
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (recordStatus == null) {
            recordStatus = RecordStatus.A;
        }
        if (delFlag == null) {
            delFlag = DelFlag.N;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}

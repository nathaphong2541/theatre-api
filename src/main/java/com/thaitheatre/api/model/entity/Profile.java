package com.thaitheatre.api.model.entity;

import java.time.Instant;

import org.hibernate.annotations.ColumnDefault;

import com.thaitheatre.api.common.DelFlag;
import com.thaitheatre.api.common.RecordStatus;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Entity
@Table(name = "profiles", uniqueConstraints = {
    @UniqueConstraint(name = "uk_profiles_user_id", columnNames = "user_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // อ้างถึงตาราง users (สมมติ users.id เป็น PK)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Options
    @Column(name = "private_profile", nullable = false)
    @ColumnDefault("false")
    private boolean privateProfile;

    @Column(name = "profile_is_company", nullable = false)
    @ColumnDefault("false")
    private boolean profileIsCompany;

    // Name
    @Column(length = 100, nullable = false)
    private String firstName;

    @Column(length = 100, nullable = false)
    private String lastName;

    @Column(length = 50)
    private String pronouns;

    // Profession
    @Column(length = 50, nullable = false)
    private String title;

    @Column(length = 50)
    private String location;

    // Contact
    @Column(length = 150, nullable = false)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 255)
    private String website;

    // Flags
    @Column(name = "multi_lang", nullable = false)
    @ColumnDefault("false")
    private boolean multiLang;

    // ใช้ Boolean wrapper เพื่อให้ null ได้ตามฟอร์ม
    @Column
    private Boolean travel;

    @Column
    private Boolean tour;

    // About/Education (text)
    @Column(columnDefinition = "text")
    private String about;

    @Column(columnDefinition = "text")
    private String education;

    // Media URLs
    @Column(length = 255)
    private String video1;

    @Column(length = 255)
    private String video2;

    // กลุ่มตัวเลข → jsonb (ARRAY ของ number)
    // ใช้ String raw JSON แล้วให้ Service แปลง Jackson (ObjectMapper) เป็น/จาก List<Integer> เมื่อรับ/ส่ง DTO
    @Column(columnDefinition = "jsonb")
    private String workLocations; // [1,2,3]

    @Column(columnDefinition = "jsonb")
    private String unions;

    @Column(columnDefinition = "jsonb")
    private String experience;

    @Column(columnDefinition = "jsonb")
    private String partners;

    @Column(columnDefinition = "jsonb")
    private String genders;

    @Column(columnDefinition = "jsonb")
    private String races;

    @Column(columnDefinition = "jsonb")
    private String additionals;

    @Column(columnDefinition = "jsonb")
    private String credits;

    // Audit
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_status", nullable = false, length = 1)
    private RecordStatus recordStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "del_flag", nullable = false, length = 1)
    private DelFlag delFlag;

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}

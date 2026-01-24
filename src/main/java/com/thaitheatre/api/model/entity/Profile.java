package com.thaitheatre.api.model.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.thaitheatre.api.common.DelFlag;
import com.thaitheatre.api.common.RecordStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.thaitheatre.api.model.dto.ProfileCredit;

@Entity
@Table(name = "profiles", uniqueConstraints = @UniqueConstraint(name = "uk_profiles_user_id", columnNames = "user_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // อ้างถึง users.id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "avatar_filename", length = 255)
    private String avatarFilename;

    // -------- Options
    @Column(name = "private_profile", nullable = false)
    @ColumnDefault("false")
    private boolean privateProfile;

    @Column(name = "profile_is_company", nullable = false)
    @ColumnDefault("false")
    private boolean profileIsCompany;

    // -------- Name (ไม่บังคับเสมอไป ปล่อยให้ validate ใน DTO/Service)
    @Column(length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    @Column(length = 50)
    private String pronouns;

    // -------- Profession
    @Column(length = 50)
    private String title;

    @Column(length = 50)
    private String location;

    // -------- Contact
    @Column(length = 150)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 255)
    private String website;

    // -------- Flags
    @Column(name = "multi_lang", nullable = false)
    @ColumnDefault("false")
    private boolean multiLang;

    // ใช้ Boolean wrapper ให้ null ได้ตามฟอร์ม
    @Column
    private Boolean travel;

    @Column
    private Boolean tour;

    // -------- About/Education
    @Column(columnDefinition = "text")
    private String about;

    @Column(columnDefinition = "text")
    private String education;

    // -------- Media URLs
    @Column(length = 255)
    private String video1;

    @Column(length = 255)
    private String video2;

    // -------- JSONB (List<Integer>) — ใช้ Hibernate 6 JSON type
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "work_locations", columnDefinition = "jsonb not null default '[]'::jsonb")
    private List<Integer> workLocations;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "unions", columnDefinition = "jsonb not null default '[]'::jsonb")
    private List<Integer> unions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "experience", columnDefinition = "jsonb not null default '[]'::jsonb")
    private List<Integer> experience;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "partners", columnDefinition = "jsonb not null default '[]'::jsonb")
    private List<Integer> partners;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "partner_detail_by_id", columnDefinition = "jsonb not null default '{}'::jsonb")
    private Map<Integer, String> partnerDetailById;

    @Column(name = "partner_other_text", length = 200)
    private String partnerOtherText;

    @Column(name = "experience_other_text", length = 200)
    private String experienceOtherText;

    @Column(name = "union_other_text", length = 200)
    private String unionOtherText;

    @Column(name = "union_student_academic_text", length = 200)
    private String unionStudentAcademicText;

    @Column(name = "gender_self_describe_text", length = 200)
    private String genderSelfDescribeText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "genders", columnDefinition = "jsonb not null default '[]'::jsonb")
    private List<Integer> genders;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "races", columnDefinition = "jsonb not null default '[]'::jsonb")
    private List<Integer> races;

    @Column(name = "racial_identity_other_text", length = 200)
    private String racialIdentityOtherText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "additionals", columnDefinition = "jsonb not null default '[]'::jsonb")
    private List<Integer> additionals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "credits", columnDefinition = "jsonb not null default '[]'::jsonb")
    private List<ProfileCredit> credits;

    @Column(name = "resume_filename")
    private String resumeFilename; // ✅ 1 resume ต่อคน

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProfilePerformance> performances = new ArrayList<>(); // ✅ รูป performance สูงสุด 6 รูป

    // -------- Social (ใหม่)
    @Column(length = 255)
    private String linkedin; // LinkedIn
    @Column(length = 255)
    private String facebook; // Facebook
    @Column(length = 255)
    private String instagram; // Instagram
    @Column(length = 255, name = "twitter")
    private String twitter; // X / Twitter

    // -------- Audit
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

    // ====== เพิ่มเองกันเหนียว (ไม่ต้องลบ @Getter ออก) ======
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        createdAt = now;
        updatedAt = now;

        // กัน NPE สำหรับลิสต์ jsonb
        if (workLocations == null) {
            workLocations = List.of();
        }
        if (unions == null) {
            unions = List.of();
        }
        if (experience == null) {
            experience = List.of();
        }
        if (partners == null) {
            partners = List.of();
        }
        if (genders == null) {
            genders = List.of();
        }
        if (races == null) {
            races = List.of();
        }
        if (additionals == null) {
            additionals = List.of();
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}

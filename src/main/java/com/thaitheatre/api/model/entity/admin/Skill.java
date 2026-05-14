package com.thaitheatre.api.model.entity.admin;

import com.thaitheatre.api.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "skill", indexes = {
        @Index(name = "idx_skill_name_th", columnList = "name_th"),
        @Index(name = "idx_skill_name_en", columnList = "name_en")
})
@Getter
@Setter
public class Skill extends BaseEntity {
    @Column(name = "name_th", nullable = false, length = 200)
    private String nameTh;

    @Column(name = "name_en", nullable = false, length = 200)
    private String nameEn;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "position_id")
    private Position position;

    @Column(length = 500)
    private String description;
}

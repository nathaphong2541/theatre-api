package com.thaitheatre.api.model.entity;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "profile_performance")
public class ProfilePerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "sort_order")
    private Integer sortOrder; // ถ้าอยากเรียงตำแหน่งใน UI

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

}

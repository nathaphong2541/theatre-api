package com.thaitheatre.api.model.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "script_pdfs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScriptPdf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // path ไปยังไฟล์ pdf บนเครื่อง / server
    @Column(nullable = false, length = 500)
    private String filePath;

    // เลขเวอร์ชัน 1,2,3,...
    @Column(nullable = false)
    private Integer versionNo;

    // ชื่อเวอร์ชัน เช่น "Draft", "Final", "Rev.2" (optional)
    @Column(length = 100)
    private String versionName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "script_id", nullable = false)
    private Script script;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private Long createdBy;
}

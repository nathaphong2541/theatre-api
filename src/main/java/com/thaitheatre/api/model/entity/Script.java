package com.thaitheatre.api.model.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "scripts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Script {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ชื่อเรื่อง
    @Column(nullable = false, length = 255)
    private String title;

    // คำอธิบาย
    @Column(columnDefinition = "TEXT")
    private String description;

    // tags เก็บเป็น string คั่นด้วย comma เช่น "ดราม่า,ตลก"
    @Column(length = 500)
    private String tags;

    // เจ้าของลิขสิทธิ์ (แสดงในหน้ารายละเอียดผลงาน) เช่น "Author's Name"
    @Column(name = "copyright_owner", length = 255)
    private String copyrightOwner;

    // หมวดของผลงาน: SCRIPT = บทละคร, RESEARCH = เรื่องเล่าและงานวิจัย
    // ค่าเริ่มต้น SCRIPT เพื่อให้ข้อมูลเดิมถูกจัดเป็นบทละครโดยอัตโนมัติ
    @Column(length = 32, nullable = false)
    @Builder.Default
    private String category = "SCRIPT";

    // ---------- Images ----------
    @Builder.Default
    @OneToMany(mappedBy = "script", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScriptImage> images = new ArrayList<>();

    // ---------- PDFs (หลายเวอร์ชัน) ----------
    @Builder.Default
    @OneToMany(mappedBy = "script", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScriptPdf> pdfs = new ArrayList<>();

    // path ไฟล์ PDF ที่เป็นบทละครหลัก (เช่น เวอร์ชันล่าสุด)
    @Column(name = "pdf_path", length = 500)
    private String pdfPath;

    // สถานะการตรวจสอบของแอดมิน: PENDING (รอตรวจ) / APPROVED (อนุมัติ) / REJECTED (ไม่อนุมัติ)
    // บทที่ยังไม่อนุมัติจะไม่ขึ้นในห้องสมุดสาธารณะ
    @Column(nullable = false, length = 20)
    private String status;

    // เหตุผลเมื่อแอดมินไม่อนุมัติ (แสดงให้เจ้าของเห็น)
    @Column(name = "review_note", length = 500)
    private String reviewNote;

    // audit fields
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    // ✅ ผูก Script.createdBy -> Profile.userId (มี unique constraint อยู่แล้ว)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "created_by", // column ในตาราง scripts
            referencedColumnName = "user_id", // column ในตาราง profiles
            insertable = false,
            updatable = false
    )

    private Profile createdProfile;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private Long updatedBy;   // user id ผู้แก้ไขล่าสุด

    @PrePersist
    void prePersist() {
        if (status == null) {
            status = "PENDING";
        }
    }

    // ───────── helper methods (สำคัญ เวลา add/remove) ─────────
    public void addImage(ScriptImage image) {
        if (image == null) {
            return;
        }
        images.add(image);
        image.setScript(this);
    }

    public void removeImage(ScriptImage image) {
        if (image == null) {
            return;
        }
        images.remove(image);
        image.setScript(null);
    }

    public void addPdf(ScriptPdf pdf) {
        if (pdf == null) {
            return;
        }
        pdfs.add(pdf);
        pdf.setScript(this);
    }

    public void removePdf(ScriptPdf pdf) {
        if (pdf == null) {
            return;
        }
        pdfs.remove(pdf);
        pdf.setScript(null);
    }
}

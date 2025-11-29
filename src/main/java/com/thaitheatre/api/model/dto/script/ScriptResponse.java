package com.thaitheatre.api.model.dto.script;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScriptResponse {

    private Long id;
    private String title;
    private String description;
    private String tags;

    private List<ScriptImageResponse> images;

    // ✅ รายการ pdf ทุกเวอร์ชัน
    private List<ScriptPdfResponse> pdfs;

    private String pdfPath;   // หรือ pdfUrl ถ้าคุณ mapping เป็น URL เวลาแสดง

    private String createdByName; // ใหม่
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;
}

package com.thaitheatre.api.model.dto.script;

import lombok.Data;

@Data
public class ScriptUpdateRequest {

    private String title;
    private String description;
    private String tags;
    private String category; // SCRIPT = บทละคร, RESEARCH = เรื่องเล่าและงานวิจัย
    private String copyrightOwner; // เจ้าของลิขสิทธิ์ (optional)
    private String pdfVersionName;
}

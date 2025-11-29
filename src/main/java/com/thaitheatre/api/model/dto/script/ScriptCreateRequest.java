package com.thaitheatre.api.model.dto.script;

import lombok.Data;

@Data
public class ScriptCreateRequest {

    private String title;
    private String description;
    private String tags; // หรือใช้ List<String> ก็ได้ถ้าจะ map เอง
    private String pdfVersionName;
}

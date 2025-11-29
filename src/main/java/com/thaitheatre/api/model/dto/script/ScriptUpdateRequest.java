package com.thaitheatre.api.model.dto.script;

import lombok.Data;

@Data
public class ScriptUpdateRequest {

    private String title;
    private String description;
    private String tags;
    private String pdfVersionName;
}

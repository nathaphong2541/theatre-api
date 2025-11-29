package com.thaitheatre.api.model.dto.script;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScriptPdfResponse {

    private Long id;
    private Integer versionNo;
    private String versionName;
    private String filePath;
    private LocalDateTime createdAt;
}

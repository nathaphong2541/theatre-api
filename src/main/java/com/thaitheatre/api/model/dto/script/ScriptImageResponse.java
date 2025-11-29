package com.thaitheatre.api.model.dto.script;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScriptImageResponse {

    private Long id;
    private String filePath;
    private Integer sortOrder;
}

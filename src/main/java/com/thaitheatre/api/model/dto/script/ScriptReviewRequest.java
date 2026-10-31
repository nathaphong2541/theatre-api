package com.thaitheatre.api.model.dto.script;

import lombok.Data;

/** Admin review decision for a script: approve or reject, with an optional note. */
@Data
public class ScriptReviewRequest {
    /** "APPROVED" or "REJECTED". */
    private String status;
    private String reviewNote;
}

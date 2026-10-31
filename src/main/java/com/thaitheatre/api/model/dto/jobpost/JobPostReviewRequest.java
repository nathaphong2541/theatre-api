package com.thaitheatre.api.model.dto.jobpost;

import lombok.Data;

/** Admin review decision: approve or reject, with an optional note. */
@Data
public class JobPostReviewRequest {
    /** "APPROVED" or "REJECTED". */
    private String status;
    private String reviewNote;
}

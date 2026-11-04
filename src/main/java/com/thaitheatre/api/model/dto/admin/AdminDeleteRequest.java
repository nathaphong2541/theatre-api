package com.thaitheatre.api.model.dto.admin;

import lombok.Data;

/** Admin delete of a moderated item (job post / script), with a reason for the log. */
@Data
public class AdminDeleteRequest {
    private String reason;
}

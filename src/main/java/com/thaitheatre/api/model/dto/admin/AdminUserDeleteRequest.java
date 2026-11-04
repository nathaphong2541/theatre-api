package com.thaitheatre.api.model.dto.admin;

import lombok.Data;

/** Soft-delete a user account, with a reason recorded in the audit log. */
@Data
public class AdminUserDeleteRequest {
    private String reason;
}

package com.thaitheatre.api.model.dto.admin;

import lombok.Data;

/** Change a user's role: "USER" or "ADMIN", with an optional reason for the log. */
@Data
public class AdminUserRoleRequest {
    private String role;
    private String reason;
}

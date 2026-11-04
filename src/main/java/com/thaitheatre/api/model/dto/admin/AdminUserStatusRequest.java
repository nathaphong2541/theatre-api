package com.thaitheatre.api.model.dto.admin;

import lombok.Data;

/** Enable or disable a user's login (active = true -> A, false -> I), with a reason. */
@Data
public class AdminUserStatusRequest {
    private boolean active;
    private String reason;
}

package com.thaitheatre.api.model.dto.admin;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

/** A user account as seen in the admin user-management list. No password. */
@Data
@Builder
public class AdminUserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    /** USER or ADMIN. */
    private String role;
    /** A = active, I = inactive (disabled login). */
    private String recordStatus;
    /** N = not deleted, Y = soft-deleted. */
    private String delFlag;
    private Instant createdAt;
    private Instant updatedAt;
    /** True for the currently signed-in admin (so the UI can self-protect). */
    private boolean self;
}

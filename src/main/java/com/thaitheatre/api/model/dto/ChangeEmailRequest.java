package com.thaitheatre.api.model.dto;

public record ChangeEmailRequest(
        String newEmail,
        String currentPassword
        ) {

}

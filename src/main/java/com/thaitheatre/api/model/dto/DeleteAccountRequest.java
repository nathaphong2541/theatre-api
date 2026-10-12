package com.thaitheatre.api.model.dto;

/** Payload for deleting the signed-in account (requires the current password). */
public record DeleteAccountRequest(
        String currentPassword
        ) {

}

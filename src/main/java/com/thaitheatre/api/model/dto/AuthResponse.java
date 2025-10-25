package com.thaitheatre.api.model.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresInSec) {

    public static AuthResponse bearer(String token, long expiresInSec) {
        return new AuthResponse(token, "Bearer", expiresInSec);
    }
}

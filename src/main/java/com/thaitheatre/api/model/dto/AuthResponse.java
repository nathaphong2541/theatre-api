package com.thaitheatre.api.model.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresInSec, Object user) {

    public static AuthResponse bearer(String token, long expiresInSec, Object user) {
        return new AuthResponse(token, "Bearer", expiresInSec, user);
    }
}

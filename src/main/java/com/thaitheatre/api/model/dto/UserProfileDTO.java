package com.thaitheatre.api.model.dto;

public record UserProfileDTO(Long id, String firstName, String lastName, String email, boolean policyConfirmed) {

}

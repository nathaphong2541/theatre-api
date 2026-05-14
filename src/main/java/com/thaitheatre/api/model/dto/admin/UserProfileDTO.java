package com.thaitheatre.api.model.dto.admin;

public record UserProfileDTO(Long id, String firstName, String lastName, String email, boolean policyConfirmed) {

}

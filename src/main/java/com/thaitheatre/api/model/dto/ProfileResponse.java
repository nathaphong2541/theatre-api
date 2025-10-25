package com.thaitheatre.api.model.dto;

import java.time.Instant;
import java.util.List;

public record ProfileResponse(
        Long id,
        Long userId,
        boolean privateProfile,
        boolean profileIsCompany,
        String firstName,
        String lastName,
        String pronouns,
        String title,
        String location,
        String email,
        String phone,
        String website,
        boolean multiLang,
        Boolean travel,
        Boolean tour,
        String about,
        String education,
        String video1,
        String video2,
        List<Integer> workLocations,
        List<Integer> unions,
        List<Integer> experience,
        List<Integer> partners,
        List<Integer> genders,
        List<Integer> races,
        List<Integer> additionals,
        List<Integer> credits,
        Instant createdAt,
        Instant updatedAt
        ) {

}

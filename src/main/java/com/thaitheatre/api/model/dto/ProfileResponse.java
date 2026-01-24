package com.thaitheatre.api.model.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

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
        String linkedin,
        String facebook,
        String instagram,
        String twitter,
        boolean multiLang,
        Boolean travel,
        Boolean tour,
        String about,
        String education,
        String video1,
        String video2,

        // ✅ เพิ่มกลับมา
        List<Integer> workLocations,

        List<Integer> partners,
        Map<Integer, String> partnerDetailById,
        String partnerOtherText,

        List<Integer> experience,
        String experienceOtherText,

        List<Integer> unions,
        String unionOtherText,
        String unionStudentAcademicText,

        List<Integer> genders,
        String genderSelfDescribeText,

        List<Integer> races,
        String racialIdentityOtherText,

        List<Integer> additionals,
        List<ProfileCredit> credits,
        Instant createdAt,
        Instant updatedAt,
        String avatarUrl,
        String resumeUrl,
        List<String> performanceUrls) {
}

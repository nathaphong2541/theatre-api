package com.thaitheatre.api.model.dto;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileRequest(

        // Options
        boolean privateProfile,
        boolean profileIsCompany,

        // Name
        @NotBlank String firstName,
        @NotBlank String lastName,
        String pronouns,

        // Profession
        @NotBlank @Size(max = 50) String title,
        @Size(max = 25) String location,

        // Contact
        @NotBlank @Email String email,
        String phone,
        String website,

        // Social
        String linkedin,
        String facebook,
        String instagram,
        String twitter,

        // Flags
        boolean multiLang,
        Boolean travel,
        Boolean tour,

        // About / Education
        String about,
        String education,

        // Media
        String video1,
        String video2,

        // ===== Lists =====
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
        List<ProfileCredit> credits) {
}

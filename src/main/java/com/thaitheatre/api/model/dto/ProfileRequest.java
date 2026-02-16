package com.thaitheatre.api.model.dto;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ProfileRequest(
        // Options
        boolean privateProfile,
        boolean profileIsCompany,
        // Name
        String firstName,
        String lastName,
        String pronouns,
        // Profession
        List<Integer> title,
        String titleOtherText,
        @Size(max = 25)
        String location,
        // Contact
        @Email
        String email,
        String phone,
        String website,
        // Social
        String linkedin,
        String facebook,
        String instagram,
        String twitter,
        // Flags
        boolean multiLang,
        List<String> additionalLanguages,
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
        String workLocationsOtherText,
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

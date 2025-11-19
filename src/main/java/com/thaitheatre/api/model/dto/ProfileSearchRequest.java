package com.thaitheatre.api.model.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProfileSearchRequest {

    // ----- free text -----
    private String q;

    // ----- flags -----
    private Boolean privateProfile;
    private Boolean profileIsCompany;
    private Boolean multiLang;
    private Boolean travel;
    private Boolean tour;

    // ----- strings -----
    private String firstName;
    private String lastName;
    private String pronouns;
    private String title;
    private String location;
    private String email;
    private String phone;
    private String website;
    private String about;
    private String education;
    private String video1;
    private String video2;

    // ----- lists -----
    private List<Integer> workLocations;
    private List<Integer> unions;
    private List<Integer> experience;
    private List<Integer> partners;
    private List<Integer> genders;
    private List<Integer> races;
    private List<Integer> additionals;

    // ----- credits filters -----
    // free text ค้นจาก company/title/venue/jobLocation
    private String creditText;

    // filter ตาม posIds / deptIds / skillIds ใน credits
    private List<Integer> creditPosIds;
    private List<Integer> creditDeptIds;
    private List<Integer> creditSkillIds;
}

package com.thaitheatre.api.model.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProfileSearchRequest {

    // ----- free text -----
    private String q;  // ค้นหลายช่องพร้อมกัน

    // ----- options / flags -----
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

    // ----- lists (ค้นแบบ "มีรายการใดรายการหนึ่งตรงกัน") -----
    private List<Integer> workLocations;
    private List<Integer> unions;
    private List<Integer> experience;
    private List<Integer> partners;
    private List<Integer> genders;
    private List<Integer> races;
    private List<Integer> additionals;
    private List<Integer> credits;
}

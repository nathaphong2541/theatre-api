package com.thaitheatre.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thaitheatre.api.common.DelFlag;
import com.thaitheatre.api.common.RecordStatus;
import com.thaitheatre.api.model.dto.ProfileRequest;
import com.thaitheatre.api.model.dto.ProfileResponse;
import com.thaitheatre.api.model.entity.Profile;
import com.thaitheatre.api.repository.ProfileRepository;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository repo;
    private final ObjectMapper om;

    @Transactional
    public ProfileResponse createOrUpdate(Long userId, ProfileRequest req) {
        var profile = repo.findByUserId(userId).orElseGet(() -> {
            var p = new Profile();
            p.setUserId(userId);
            // ✅ โปรไฟล์ที่สร้างใหม่ ตั้งสถานะเริ่มต้นเสมอ
            p.setRecordStatus(RecordStatus.A);
            p.setDelFlag(DelFlag.N);
            return p;
        });

        // map fields
        profile.setPrivateProfile(req.privateProfile());
        profile.setProfileIsCompany(req.profileIsCompany());

        profile.setFirstName(req.firstName());
        profile.setLastName(req.lastName());
        profile.setPronouns(req.pronouns());

        profile.setTitle(req.title());
        profile.setLocation(req.location());

        profile.setEmail(req.email());
        profile.setPhone(req.phone());
        profile.setWebsite(req.website());

        profile.setMultiLang(req.multiLang());
        profile.setTravel(req.travel());
        profile.setTour(req.tour());

        profile.setAbout(req.about());
        profile.setEducation(req.education());

        profile.setVideo1(req.video1());
        profile.setVideo2(req.video2());

        profile.setWorkLocations(writeJson(req.workLocations()));
        profile.setUnions(writeJson(req.unions()));
        profile.setExperience(writeJson(req.experience()));
        profile.setPartners(writeJson(req.partners()));
        profile.setGenders(writeJson(req.genders()));
        profile.setRaces(writeJson(req.races()));
        profile.setAdditionals(writeJson(req.additionals()));
        profile.setCredits(writeJson(req.credits()));

        var saved = repo.save(profile);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getMy(Long userId) {
        var p = repo.findByUserId(userId).orElseThrow(() -> new IllegalStateException("Profile not found"));
        return toResponse(p);
    }

    private String writeJson(List<Integer> list) {
        try {
            return list == null ? "[]" : om.writeValueAsString(list);
        } catch (Exception e) {
            throw new RuntimeException("Cannot serialize list", e);
        }
    }

    private List<Integer> readJson(String json) {
        try {
            return json == null ? List.of() : om.readValue(json, new TypeReference<List<Integer>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Cannot deserialize list", e);
        }
    }

    private ProfileResponse toResponse(Profile p) {
        return new ProfileResponse(
                p.getId(),
                p.getUserId(),
                p.isPrivateProfile(),
                p.isProfileIsCompany(),
                p.getFirstName(),
                p.getLastName(),
                p.getPronouns(),
                p.getTitle(),
                p.getLocation(),
                p.getEmail(),
                p.getPhone(),
                p.getWebsite(),
                p.isMultiLang(),
                p.getTravel(),
                p.getTour(),
                p.getAbout(),
                p.getEducation(),
                p.getVideo1(),
                p.getVideo2(),
                readJson(p.getWorkLocations()),
                readJson(p.getUnions()),
                readJson(p.getExperience()),
                readJson(p.getPartners()),
                readJson(p.getGenders()),
                readJson(p.getRaces()),
                readJson(p.getAdditionals()),
                readJson(p.getCredits()),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}

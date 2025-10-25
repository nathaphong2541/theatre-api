package com.thaitheatre.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thaitheatre.api.common.DelFlag;
import com.thaitheatre.api.common.RecordStatus;
import com.thaitheatre.api.model.dto.ProfileRequest;
import com.thaitheatre.api.model.dto.ProfileResponse;
import com.thaitheatre.api.model.entity.Profile;
import com.thaitheatre.api.repository.ProfileRepository;     // 👈 สมมุติชื่อเอนทิตีผู้ใช้
import com.thaitheatre.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;    // 👈 เพิ่ม repo ผู้ใช้

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository repo;
    private final UserRepository userRepo;              // 👈 ฉีดเข้ามา

    @Transactional
    public ProfileResponse createOrUpdate(Long userId, ProfileRequest req) {
        var profile = repo.findByUserId(userId).orElseGet(() -> {
            var p = new Profile();
            p.setUserId(userId);
            p.setRecordStatus(RecordStatus.A);
            p.setDelFlag(DelFlag.N);
            // กัน NPE สำหรับลิสต์ jsonb (เผื่อ DB ยังไม่มี DEFAULT)
            p.setWorkLocations(List.of());
            p.setUnions(List.of());
            p.setExperience(List.of());
            p.setPartners(List.of());
            p.setGenders(List.of());
            p.setRaces(List.of());
            p.setAdditionals(List.of());
            p.setCredits(List.of());
            return p;
        });

        // map fields (primitive boolean โอเคอยู่แล้ว)
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

        // ✅ jsonb: set เป็น List<Integer> ตรง ๆ
        profile.setWorkLocations(nvl(req.workLocations()));
        profile.setUnions(nvl(req.unions()));
        profile.setExperience(nvl(req.experience()));
        profile.setPartners(nvl(req.partners()));
        profile.setGenders(nvl(req.genders()));
        profile.setRaces(nvl(req.races()));
        profile.setAdditionals(nvl(req.additionals()));
        profile.setCredits(nvl(req.credits()));

        var saved = repo.save(profile);

        // ✅ อัปเดตชื่อ–นามสกุลในตาราง users ภายใต้ทรานแซกชันเดียวกัน
        syncUserName(userId, req.firstName(), req.lastName());

        return toResponse(saved);
    }

    @Transactional
    public ProfileResponse getMy(Long userId) {
        var p = repo.findByUserId(userId).orElseGet(() -> {
            var np = new Profile();
            np.setUserId(userId);
            np.setRecordStatus(RecordStatus.A);
            np.setDelFlag(DelFlag.N);
            // ค่าเริ่มต้นลิสต์ว่างเพื่อความปลอดภัย
            np.setWorkLocations(List.of());
            np.setUnions(List.of());
            np.setExperience(List.of());
            np.setPartners(List.of());
            np.setGenders(List.of());
            np.setRaces(List.of());
            np.setAdditionals(List.of());
            np.setCredits(List.of());
            return repo.save(np);
        });
        return toResponse(p);
    }

    /**
     * อัปเดต first_name / last_name ในตาราง users เฉพาะเมื่อมีค่าใหม่และแตกต่าง
     */
    private void syncUserName(Long userId, String firstName, String lastName) {
        var user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found for id=" + userId));

        boolean changed = false;

        if (firstName != null) {
            var fn = firstName.trim();
            if (!fn.isEmpty() && !fn.equals(user.getFirstName())) {
                user.setFirstName(fn);
                changed = true;
            }
        }
        if (lastName != null) {
            var ln = lastName.trim();
            if (!ln.isEmpty() && !ln.equals(user.getLastName())) {
                user.setLastName(ln);
                changed = true;
            }
        }

        if (changed) {
            userRepo.save(user);
        }
    }

    private static List<Integer> nvl(List<Integer> v) {
        return v == null ? List.of() : v;
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
                nvl(p.getWorkLocations()),
                nvl(p.getUnions()),
                nvl(p.getExperience()),
                nvl(p.getPartners()),
                nvl(p.getGenders()),
                nvl(p.getRaces()),
                nvl(p.getAdditionals()),
                nvl(p.getCredits()),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}

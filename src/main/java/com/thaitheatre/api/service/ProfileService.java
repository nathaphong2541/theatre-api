package com.thaitheatre.api.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.thaitheatre.api.common.DelFlag;
import com.thaitheatre.api.common.RecordStatus;
import com.thaitheatre.api.model.dto.ProfileRequest;
import com.thaitheatre.api.model.dto.ProfileResponse;
import com.thaitheatre.api.model.entity.Profile;
import com.thaitheatre.api.repository.ProfileRepository;
import com.thaitheatre.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository repo;
    private final UserRepository userRepo;
    private final FileStorageService fileStorageService;

    @Value("${app.files.public-base-url}")
    private String publicBaseUrl;

    @Value("${app.files.profile-dir}")
    private String profileDir; // ใช้ตอนลบไฟล์จริง

    // ===== JSON only (ไม่ยุ่งกับรูป) =====
    @Transactional
    public ProfileResponse createOrUpdate(Long userId, ProfileRequest req) {
        return createOrUpdate(userId, req, null);
    }

    // ===== JSON + รูป (multipart) =====
    @Transactional
    public ProfileResponse createOrUpdate(Long userId, ProfileRequest req, MultipartFile avatar) {
        var profile = repo.findByUserId(userId).orElseGet(() -> {
            var p = new Profile();
            p.setUserId(userId);
            p.setRecordStatus(RecordStatus.A);
            p.setDelFlag(DelFlag.N);
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

        // map fields
        if (req != null) {
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
            profile.setWorkLocations(nvl(req.workLocations()));
            profile.setUnions(nvl(req.unions()));
            profile.setExperience(nvl(req.experience()));
            profile.setPartners(nvl(req.partners()));
            profile.setGenders(nvl(req.genders()));
            profile.setRaces(nvl(req.races()));
            profile.setAdditionals(nvl(req.additionals()));
            profile.setCredits(nvl(req.credits()));
        }

        // ✅ จัดการไฟล์รูป (เฉพาะเมื่อมีไฟล์ส่งมา)
        if (avatar != null && !avatar.isEmpty()) {
            try {
                String newName = fileStorageService.saveProfileImage(avatar, profile.getAvatarFilename());
                profile.setAvatarFilename(newName);
            } catch (IllegalArgumentException e) {
                throw e; // นามสกุลไม่ถูกต้อง
            } catch (Exception e) {
                throw new RuntimeException("อัปโหลดรูปไม่สำเร็จ", e);
            }
        }

        var saved = repo.save(profile);

        // อัปเดตชื่อจริง/นามสกุลใน users (เฉพาะกรณี req != null)
        if (req != null) {
            syncUserName(userId, req.firstName(), req.lastName());
        }

        return toResponse(saved);
    }

    // ===== เปลี่ยนรูปอย่างเดียว =====
    @Transactional
    public ProfileResponse updateAvatarOnly(Long userId, MultipartFile avatar) {
        if (avatar == null || avatar.isEmpty()) {
            throw new IllegalArgumentException("กรุณาเลือกไฟล์รูป");
        }
        var profile = repo.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Profile not found for userId=" + userId));

        try {
            String newName = fileStorageService.saveProfileImage(avatar, profile.getAvatarFilename());
            profile.setAvatarFilename(newName);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("อัปโหลดรูปไม่สำเร็จ", e);
        }

        var saved = repo.save(profile);
        return toResponse(saved);
    }

    // ===== ลบรูปโปรไฟล์ =====
    @Transactional
    public ProfileResponse deleteAvatar(Long userId) {
        var profile = repo.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Profile not found for userId=" + userId));

        String old = profile.getAvatarFilename();
        profile.setAvatarFilename(null);
        var saved = repo.save(profile);

        // ลบไฟล์บนดิสก์ (ไม่ทำให้ธุรกรรม DB ล้ม ถ้าลบไฟล์พลาด)
        if (old != null && !old.isBlank()) {
            try {
                Path p = Paths.get(profileDir).resolve(old);
                Files.deleteIfExists(p);
            } catch (Exception ignored) {
            }
        }
        return toResponse(saved);
    }

    @Transactional
    public ProfileResponse getMy(Long userId) {
        var p = repo.findByUserId(userId).orElseGet(() -> {
            var np = new Profile();
            np.setUserId(userId);
            np.setRecordStatus(RecordStatus.A);
            np.setDelFlag(DelFlag.N);
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

    // -------------------- helpers --------------------
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
        // หมายเหตุ: ถ้าตารางเป็น jsonb ที่อาจเก็บ null/[] ได้
    }

    public ProfileResponse toResponse(Profile p) {
        String avatarUrl = null;
        if (p.getAvatarFilename() != null && !p.getAvatarFilename().isBlank()) {
            avatarUrl = publicBaseUrl.endsWith("/")
                    ? publicBaseUrl + p.getAvatarFilename()
                    : publicBaseUrl + "/" + p.getAvatarFilename();
        }
        return new ProfileResponse(
                p.getId(), p.getUserId(), p.isPrivateProfile(), p.isProfileIsCompany(),
                p.getFirstName(), p.getLastName(), p.getPronouns(), p.getTitle(),
                p.getLocation(), p.getEmail(), p.getPhone(), p.getWebsite(),
                p.isMultiLang(), p.getTravel(), p.getTour(), p.getAbout(), p.getEducation(),
                p.getVideo1(), p.getVideo2(),
                nvl(p.getWorkLocations()), nvl(p.getUnions()), nvl(p.getExperience()),
                nvl(p.getPartners()), nvl(p.getGenders()), nvl(p.getRaces()),
                nvl(p.getAdditionals()), nvl(p.getCredits()),
                p.getCreatedAt(), p.getUpdatedAt(), avatarUrl
        );
    }

    @Transactional(readOnly = true)
    public ProfileResponse getByUserIdPublic(Long userId) {
        return repo.findByUserId(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }
}

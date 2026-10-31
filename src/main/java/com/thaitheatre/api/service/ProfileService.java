package com.thaitheatre.api.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.thaitheatre.api.common.DelFlag;
import com.thaitheatre.api.common.RecordStatus;
import com.thaitheatre.api.model.dto.ProfileCredit;
import com.thaitheatre.api.model.dto.ProfilePerformanceItem;
import com.thaitheatre.api.model.dto.ProfileRequest;
import com.thaitheatre.api.model.dto.ProfileResponse;
import com.thaitheatre.api.model.entity.Profile;
import com.thaitheatre.api.model.entity.ProfilePerformance;
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
    private String profileDir;

    // ✅ ใช้ id เดียวพอ
    private static final int OTHER_ID = 999;

    // ===== JSON only =====
    @Transactional
    public ProfileResponse createOrUpdate(Long userId, ProfileRequest req) {
        return createOrUpdate(userId, req, null);
    }

    // ===== JSON + avatar (multipart) =====
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
            p.setSpecialSkills(List.of());
            p.setPartners(List.of());
            p.setGenders(List.of());
            p.setRaces(List.of());
            p.setAdditionals(List.of());
            p.setCredits(List.of());
            p.setPartnerDetailById(Map.of());
            p.setPublicVisibility(Map.of());
            return p;
        });

        if (req != null) {
            // base
            profile.setPrivateProfile(req.privateProfile());
            profile.setProfileIsCompany(req.profileIsCompany());
            profile.setFirstName(req.firstName());
            profile.setLastName(req.lastName());
            profile.setPronouns(req.pronouns());
            profile.setTitle(nvl(req.title()));
            profile.setLocation(req.location());
            profile.setEmail(req.email());
            profile.setPhone(req.phone());
            profile.setWebsite(req.website());
            profile.setAdditionalLanguages(cleanLanguages(req.additionalLanguages()));
            // social
            profile.setLinkedin(req.linkedin());
            profile.setFacebook(req.facebook());
            profile.setInstagram(req.instagram());
            profile.setTwitter(req.twitter());

            // flags + about
            profile.setMultiLang(req.multiLang());
            profile.setTravel(req.travel());
            profile.setTour(req.tour());
            profile.setAbout(req.about());
            profile.setEducation(req.education());
            profile.setVideo1(req.video1());
            profile.setVideo2(req.video2());

            // lists
            profile.setWorkLocations(nvl(req.workLocations()));
            profile.setUnions(nvl(req.unions()));
            profile.setExperience(nvl(req.experience()));
            profile.setSpecialSkills(nvl(req.specialSkills()));
            profile.setPartners(nvl(req.partners()));
            profile.setGenders(nvl(req.genders()));
            profile.setRaces(nvl(req.races()));
            profile.setAdditionals(nvl(req.additionals()));
            profile.setCredits(validateAndCleanCredits(req.credits()));

            // texts (trim)
            profile.setTitleOtherText(trimToNull(req.titleOtherText()));
            profile.setWorklocaltionsOtherText(trimToNull(req.workLocationsOtherText()));
            profile.setGenderSelfDescribeText(trimToNull(req.genderSelfDescribeText()));
            profile.setPartnerOtherText(trimToNull(req.partnerOtherText()));
            profile.setExperienceOtherText(trimToNull(req.experienceOtherText()));
            profile.setSpecialSkillsOtherText(trimToNull(req.specialSkillsOtherText()));
            profile.setUnionOtherText(trimToNull(req.unionOtherText()));
            profile.setUnionStudentAcademicText(trimToNull(req.unionStudentAcademicText()));

            // map
            profile.setPartnerDetailById(req.partnerDetailById() == null ? Map.of() : req.partnerDetailById());

            // per-section public visibility toggles (default empty = all private)
            profile.setPublicVisibility(req.publicVisibility() == null ? Map.of() : req.publicVisibility());

            // ✅ validate + cleanup
            applyExtraTexts(profile, req);
        }

        // avatar upload
        if (avatar != null && !avatar.isEmpty()) {
            try {
                String newName = fileStorageService.saveProfileImage(avatar, profile.getAvatarFilename());
                profile.setAvatarFilename(newName);
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("อัปโหลดรูปไม่สำเร็จ", e);
            }
        }

        var saved = repo.save(profile);

        if (req != null) {
            syncUserName(userId, req.firstName(), req.lastName());
        }

        return toResponse(saved);
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

    private static <T> List<T> nvl(List<T> v) {
        return v == null ? List.of() : v;
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        var t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * ✅ Validate เฉพาะ 999 (Other/self-describe) และเคลียร์ text ถ้าไม่ได้เลือก
     */
    /**
     * ✅ Validate เฉพาะ 999 (Other/self-describe) และเคลียร์ text ถ้าไม่ได้เลือก
     */
    private void applyExtraTexts(Profile profile, ProfileRequest req) {

        // workLocations -> other text
        if (contains(profile.getWorkLocations(), OTHER_ID)) {
            if (profile.getWorklocaltionsOtherText() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "workLocationsOtherText is required when workLocations contains " + OTHER_ID);
            }
        } else {
            profile.setWorklocaltionsOtherText(null); // ✅ ถูกต้อง
        }

        if (contains(profile.getTitle(), OTHER_ID)) {
            if (profile.getTitleOtherText() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "titleOtherText is required when title contains " + OTHER_ID);
            }
        } else {
            profile.setTitleOtherText(null); // ✅ ถูกต้อง
        }

        // genders -> self describe
        if (contains(profile.getGenders(), OTHER_ID)) {
            if (profile.getGenderSelfDescribeText() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "genderSelfDescribeText is required when genders contains " + OTHER_ID);
            }
        } else {
            profile.setGenderSelfDescribeText(null);
        }

        // experience -> other
        if (contains(profile.getExperience(), OTHER_ID)) {
            if (profile.getExperienceOtherText() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "experienceOtherText is required when experience contains " + OTHER_ID);
            }
        } else {
            profile.setExperienceOtherText(null);
        }

        // specialSkills -> other
        if (contains(profile.getSpecialSkills(), OTHER_ID)) {
            if (profile.getSpecialSkillsOtherText() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "specialSkillsOtherText is required when specialSkills contains " + OTHER_ID);
            }
        } else {
            profile.setSpecialSkillsOtherText(null);
        }

        // unions -> other
        if (contains(profile.getUnions(), OTHER_ID)) {
            if (profile.getUnionOtherText() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "unionOtherText is required when unions contains " + OTHER_ID);
            }
        } else {
            profile.setUnionOtherText(null);
        }

        // partners -> other
        if (contains(profile.getPartners(), OTHER_ID)) {
            if (profile.getPartnerOtherText() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "partnerOtherText is required when partners contains " + OTHER_ID);
            }
        } else {
            profile.setPartnerOtherText(null);
        }

        // races -> other
        if (contains(profile.getRaces(), OTHER_ID)) {
            String t = trimToNull(req.racialIdentityOtherText());
            if (t == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "racialIdentityOtherText is required when races contains " + OTHER_ID);
            }
            profile.setRacialIdentityOtherText(t);
        } else {
            profile.setRacialIdentityOtherText(null);
        }

        // ✅ multiLang -> additionalLanguages
        if (profile.isMultiLang()) {
            var langs = cleanLanguages(req.additionalLanguages());
            profile.setAdditionalLanguages(langs);
            // ถ้าต้องการบังคับว่าต้องมีอย่างน้อย 1 ภาษาเมื่อ multiLang=true:
            // if (langs.isEmpty()) throw new
            // ResponseStatusException(HttpStatus.BAD_REQUEST,
            // "additionalLanguages is required when multiLang is true");
        } else {
            profile.setAdditionalLanguages(List.of());
        }

        // ✅ clean partnerDetailById: keep เฉพาะ partner ที่ถูกเลือก
        Map<Integer, String> map = profile.getPartnerDetailById();
        if (map == null) {
            map = Map.of();
        }

        var keep = new HashSet<>(nvl(profile.getPartners()));
        var cleaned = new HashMap<Integer, String>();

        for (var e : map.entrySet()) {
            Integer k = e.getKey();
            String v = trimToNull(e.getValue());
            if (k != null && keep.contains(k) && v != null) {
                cleaned.put(k, v);
            }
        }
        profile.setPartnerDetailById(cleaned);

    }

    private List<ProfileCredit> validateAndCleanCredits(List<ProfileCredit> credits) {

        if (credits == null) {
            return List.of();
        }

        var result = new java.util.ArrayList<ProfileCredit>();

        for (int i = 0; i < credits.size(); i++) {

            var c = credits.get(i);

            var deptIds = nvl(c.deptIds());
            var posIds = nvl(c.posIds());

            String deptText = trimToNull(c.deptText());
            String posText = trimToNull(c.posText());

            // ---- deptIds 999 ----
            if (deptIds.contains(OTHER_ID)) {
                if (deptText == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "credits[" + i + "].deptText is required when deptIds contains " + OTHER_ID
                    );
                }
            } else {
                deptText = null;
            }

            // ---- posIds 999 ----
            if (posIds.contains(OTHER_ID)) {
                if (posText == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "credits[" + i + "].posText is required when posIds contains " + OTHER_ID
                    );
                }
            } else {
                posText = null;
            }

            result.add(new ProfileCredit(
                    c.company(),
                    c.title(),
                    c.startYear(),
                    c.endYear(),
                    c.current(),
                    c.venue(),
                    c.jobLocation(),
                    c.internship(),
                    c.fellowship(),
                    deptIds,
                    deptText,
                    posIds,
                    posText,
                    nvl(c.skillIds())
            ));
        }

        return result;
    }

    private static boolean contains(List<Integer> list, int id) {
        return list != null && list.contains(id);
    }

    // -------------------- response --------------------
    public ProfileResponse toResponse(Profile p) {
        String avatarUrl = null;
        if (p.getAvatarFilename() != null && !p.getAvatarFilename().isBlank()) {
            avatarUrl = buildUrl(p.getAvatarFilename());
        }

        String resumeUrl = null;
        if (p.getResumeFilename() != null && !p.getResumeFilename().isBlank()) {
            resumeUrl = buildUrl("resume/" + p.getResumeFilename());
        }

        // ✅ sort ให้แน่นอนตาม sortOrder (ถ้ามี)
        var perfs = p.getPerformances().stream()
                .sorted(Comparator.comparing(ProfilePerformance::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .toList();

        List<String> performanceUrls = perfs.stream()
                .map(perf -> buildUrl("performance/" + perf.getFilename()))
                .toList();

        List<ProfilePerformanceItem> performanceItems = perfs.stream()
                .map(perf -> new ProfilePerformanceItem(
                perf.getId(),
                buildUrl("performance/" + perf.getFilename()),
                perf.getSortOrder()
        ))
                .toList();

        return new ProfileResponse(
                p.getId(),
                p.getUserId(),
                p.isPrivateProfile(),
                p.isProfileIsCompany(),
                p.getFirstName(),
                p.getLastName(),
                p.getPronouns(),
                nvl(p.getTitle()),
                p.getTitleOtherText(),
                p.getLocation(),
                p.getEmail(),
                p.getPhone(),
                p.getWebsite(),
                p.getLinkedin(),
                p.getFacebook(),
                p.getInstagram(),
                p.getTwitter(),
                p.isMultiLang(),
                nvlStr(p.getAdditionalLanguages()),
                p.getTravel(),
                p.getTour(),
                p.getAbout(),
                p.getEducation(),
                p.getVideo1(),
                p.getVideo2(),
                nvl(p.getWorkLocations()),
                p.getWorklocaltionsOtherText(),
                nvl(p.getPartners()),
                p.getPartnerDetailById() == null ? Map.of() : p.getPartnerDetailById(),
                p.getPartnerOtherText(),
                nvl(p.getExperience()),
                p.getExperienceOtherText(),
                nvl(p.getSpecialSkills()),
                p.getSpecialSkillsOtherText(),
                nvl(p.getUnions()),
                p.getUnionOtherText(),
                p.getUnionStudentAcademicText(),
                nvl(p.getGenders()),
                p.getGenderSelfDescribeText(),
                nvl(p.getRaces()),
                p.getRacialIdentityOtherText(),
                nvl(p.getAdditionals()),
                nvl(p.getCredits()),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                avatarUrl,
                resumeUrl,
                performanceUrls,
                performanceItems,
                p.getPublicVisibility() == null ? Map.of() : p.getPublicVisibility()
        );
    }

    // -------------------- PUBLIC response (visibility-filtered) --------------------

    /**
     * Whether a section is shown publicly. Default is SHOWN: a section is only
     * hidden when the member explicitly turned its toggle off (value == false).
     * Absent/true => shown. This keeps existing profiles (which have no saved
     * toggles yet) fully visible until the owner opts a section out.
     */
    private static boolean vis(Profile p, String key) {
        var m = p.getPublicVisibility();
        return m == null || !Boolean.FALSE.equals(m.get(key));
    }

    /**
     * Response for ANONYMOUS / other-user callers (directory list + public
     * profile). Only sections the member explicitly toggled public are filled;
     * everything else is blanked. The login email (users.email) is never here —
     * only the separate contact email (profiles.email), and only if toggled on.
     *
     * gender/identity, phone, etc. are each gated by their own toggle, so no
     * personal data leaks unless the member chose to show it.
     */
    public ProfileResponse toPublicResponse(Profile p) {
        String avatarUrl = null;
        if (p.getAvatarFilename() != null && !p.getAvatarFilename().isBlank()) {
            avatarUrl = buildUrl(p.getAvatarFilename());
        }

        boolean showResume = vis(p, "resume");
        String resumeUrl = null;
        if (showResume && p.getResumeFilename() != null && !p.getResumeFilename().isBlank()) {
            resumeUrl = buildUrl("resume/" + p.getResumeFilename());
        }

        boolean showMedia = vis(p, "media");
        var perfs = showMedia
                ? p.getPerformances().stream()
                        .sorted(Comparator.comparing(ProfilePerformance::getSortOrder,
                                Comparator.nullsLast(Integer::compareTo)))
                        .toList()
                : java.util.List.<ProfilePerformance>of();
        List<String> performanceUrls = perfs.stream()
                .map(perf -> buildUrl("performance/" + perf.getFilename()))
                .toList();
        List<ProfilePerformanceItem> performanceItems = perfs.stream()
                .map(perf -> new ProfilePerformanceItem(perf.getId(),
                        buildUrl("performance/" + perf.getFilename()), perf.getSortOrder()))
                .toList();

        boolean showProfession = vis(p, "profession");
        boolean showAbout = vis(p, "about");
        boolean showContact = vis(p, "email");
        boolean showPhone = vis(p, "phone");
        boolean showWebsite = vis(p, "website");
        boolean showSocial = vis(p, "social");
        boolean showEducation = vis(p, "education");
        boolean showWorkLoc = vis(p, "workLocations");
        boolean showPartners = vis(p, "partners");
        boolean showExperience = vis(p, "experience");
        boolean showSpecialSkills = vis(p, "specialSkills");
        boolean showUnions = vis(p, "unions");
        boolean showGenders = vis(p, "genders");
        boolean showRaces = vis(p, "races");
        boolean showLanguages = vis(p, "languages");
        boolean showCredits = vis(p, "credits");

        return new ProfileResponse(
                p.getId(),
                p.getUserId(),
                p.isPrivateProfile(),
                p.isProfileIsCompany(),
                // Name + avatar stay visible (they identify the public card).
                p.getFirstName(),
                p.getLastName(),
                p.getPronouns(),
                showProfession ? nvl(p.getTitle()) : List.of(),
                showProfession ? p.getTitleOtherText() : null,
                p.getLocation(),
                // Contact email: the profile's own contact email, ONLY if toggled.
                // NEVER the login email (that lives on UserAccount, not here).
                showContact ? p.getEmail() : null,
                showPhone ? p.getPhone() : null,
                showWebsite ? p.getWebsite() : null,
                showSocial ? p.getLinkedin() : null,
                showSocial ? p.getFacebook() : null,
                showSocial ? p.getInstagram() : null,
                showSocial ? p.getTwitter() : null,
                p.isMultiLang(),
                showLanguages ? nvlStr(p.getAdditionalLanguages()) : List.of(),
                p.getTravel(),
                p.getTour(),
                showAbout ? p.getAbout() : null,
                showEducation ? p.getEducation() : null,
                showMedia ? p.getVideo1() : null,
                showMedia ? p.getVideo2() : null,
                showWorkLoc ? nvl(p.getWorkLocations()) : List.of(),
                showWorkLoc ? p.getWorklocaltionsOtherText() : null,
                showPartners ? nvl(p.getPartners()) : List.of(),
                showPartners ? (p.getPartnerDetailById() == null ? Map.of() : p.getPartnerDetailById()) : Map.of(),
                showPartners ? p.getPartnerOtherText() : null,
                showExperience ? nvl(p.getExperience()) : List.of(),
                showExperience ? p.getExperienceOtherText() : null,
                showSpecialSkills ? nvl(p.getSpecialSkills()) : List.of(),
                showSpecialSkills ? p.getSpecialSkillsOtherText() : null,
                showUnions ? nvl(p.getUnions()) : List.of(),
                showUnions ? p.getUnionOtherText() : null,
                showUnions ? p.getUnionStudentAcademicText() : null,
                showGenders ? nvl(p.getGenders()) : List.of(),
                showGenders ? p.getGenderSelfDescribeText() : null,
                showRaces ? nvl(p.getRaces()) : List.of(),
                showRaces ? p.getRacialIdentityOtherText() : null,
                nvl(p.getAdditionals()),
                showCredits ? nvl(p.getCredits()) : List.of(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                avatarUrl,
                resumeUrl,
                performanceUrls,
                performanceItems,
                p.getPublicVisibility() == null ? Map.of() : p.getPublicVisibility()
        );
    }

    private static List<String> nvlStr(List<String> v) {
        return v == null ? List.of() : v;
    }

    private String buildUrl(String filename) {
        return publicBaseUrl.endsWith("/")
                ? publicBaseUrl + filename
                : publicBaseUrl + "/" + filename;
    }

    // ---- the rest (resume/performance/avatar) ใช้ของเดิมได้ ----
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
            np.setPartnerDetailById(Map.of());
            np.setAdditionalLanguages(List.of());
            // ดึงชื่อจาก user มาใส่ profile
            userRepo.findById(userId).ifPresent(u -> {
                np.setFirstName(u.getFirstName());
                np.setLastName(u.getLastName());
                np.setEmail(u.getEmail());
            });
            return repo.save(np);
        });
        return toResponse(p);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getByUserIdPublic(Long userId) {
        var profile = repo.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        // Privacy gate: a private profile is only viewable by its owner.
        // For anyone else (anonymous or a different user) we return 404 so we
        // don't leak the existence of the profile.
        boolean owner = isCurrentUser(userId);
        if (profile.isPrivateProfile() && !owner) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found");
        }

        // Owner sees everything; anyone else sees only the sections they made public.
        return owner ? toResponse(profile) : toPublicResponse(profile);
    }

    /** True when the authenticated caller owns the given profile userId. */
    private boolean isCurrentUser(Long userId) {
        if (userId == null) {
            return false;
        }
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return false;
        }
        return userRepo.findByEmail(auth.getName())
                .map(u -> userId.equals(u.getId()))
                .orElse(false);
    }

    @Transactional
    public ProfileResponse updateResume(Long userId, MultipartFile resume) {
        if (resume == null || resume.isEmpty()) {
            throw new IllegalArgumentException("กรุณาเลือกไฟล์ resume");
        }

        var profile = repo.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Profile not found for userId=" + userId));

        try {
            String newName = fileStorageService.saveResume(resume, profile.getResumeFilename());
            profile.setResumeFilename(newName);
        } catch (IllegalArgumentException e) {
            // นามสกุลผิด / size เกิน ให้โยนต่อเป็น 400
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("อัปโหลด resume ไม่สำเร็จ", e);
        }

        var saved = repo.save(profile);
        return toResponse(saved);
    }

    @Transactional
    public ProfileResponse deleteResume(Long userId) {
        var profile = repo.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Profile not found for userId=" + userId));

        String old = profile.getResumeFilename();
        profile.setResumeFilename(null);
        var saved = repo.save(profile);

        if (old != null && !old.isBlank()) {
            try {
                Path p = Paths.get(profileDir, "resume").resolve(old);
                Files.deleteIfExists(p);
            } catch (Exception ignored) {
            }
        }
        return toResponse(saved);
    }

    private static final int MAX_PERFORMANCE_PER_PROFILE = 6;

    @Transactional
    public ProfileResponse addPerformances(Long userId, MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("กรุณาเลือกไฟล์ performance อย่างน้อย 1 ไฟล์");
        }

        var profile = repo.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Profile not found for userId=" + userId));

        int current = profile.getPerformances().size();
        if (current + files.length > MAX_PERFORMANCE_PER_PROFILE) {
            throw new IllegalArgumentException(
                    "อัปโหลด performance ได้ไม่เกิน " + MAX_PERFORMANCE_PER_PROFILE + " ไฟล์ต่อคน");
        }

        int baseOrder = current; // ใช้เป็น sortOrder ต่อจากของเดิม

        for (int i = 0; i < files.length; i++) {
            MultipartFile f = files[i];
            if (f == null || f.isEmpty()) {
                continue;
            }

            try {
                String filename = fileStorageService.savePerformanceImage(f);
                var perf = new ProfilePerformance();
                perf.setProfile(profile);
                perf.setFilename(filename);
                perf.setSortOrder(baseOrder + i);
                profile.getPerformances().add(perf);
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("อัปโหลด performance ไม่สำเร็จ", e);
            }
        }

        var saved = repo.save(profile);
        return toResponse(saved);
    }

    @Transactional
    public ProfileResponse deletePerformance(Long userId, Long performanceId) {
        var profile = repo.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Profile not found for userId=" + userId));

        var it = profile.getPerformances().iterator();
        String filenameToDelete = null;
        boolean found = false;

        while (it.hasNext()) {
            var perf = it.next();
            if (perf.getId().equals(performanceId)) {
                filenameToDelete = perf.getFilename();
                it.remove(); // orphanRemoval = true
                found = true;
                break;
            }
        }

        if (!found) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Performance not found: " + performanceId);
        }

        var saved = repo.save(profile);

        if (filenameToDelete != null) {
            try {
                Path p = Paths.get(profileDir, "performance").resolve(filenameToDelete);
                Files.deleteIfExists(p);
            } catch (Exception ignored) {
            }
        }

        // ✅ (optional) re-order sortOrder ให้ต่อเนื่องหลังลบ
        for (int i = 0; i < saved.getPerformances().size(); i++) {
            saved.getPerformances().get(i).setSortOrder(i);
        }
        saved = repo.save(saved);

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

    private static List<String> cleanLanguages(List<String> langs) {
        if (langs == null) {
            return List.of();
        }

        // trim + ตัดว่าง + unique (รักษาลำดับ)
        var seen = new HashSet<String>();
        var out = new java.util.ArrayList<String>();

        for (String s : langs) {
            if (s == null) {
                continue;
            }
            var t = s.trim();
            if (t.isEmpty()) {
                continue;
            }
            if (t.length() > 50) {
                t = t.substring(0, 50); // กันยาวเกิน (หรือโยน 400 ก็ได้)

            }
            var key = t.toLowerCase();
            if (seen.add(key)) {
                out.add(t);
            }
        }
        return out;
    }

}

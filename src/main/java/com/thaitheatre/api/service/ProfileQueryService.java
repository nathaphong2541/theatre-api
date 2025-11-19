package com.thaitheatre.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.thaitheatre.api.model.dto.ProfileResponse;
import com.thaitheatre.api.model.dto.ProfileSearchRequest;
import com.thaitheatre.api.model.entity.Profile;
import com.thaitheatre.api.repository.ProfileRepository;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileQueryService {

    private final ProfileRepository repo;
    private final ProfileService profileService;

    public Page<ProfileResponse> search(ProfileSearchRequest rq, Pageable pageable) {
        if (rq == null) {
            rq = new ProfileSearchRequest();
        }

        // default pageable
        if (pageable == null) {
            pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "updatedAt"));
        } else if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "updatedAt"));
        }

        Specification<Profile> spec = Specification.where((root, cq, cb) -> {
            cq.distinct(true);
            return cb.conjunction();
        });

        // ---------- free text q (ไม่แตะ credits เลย เพื่อกันปัญหา) ----------
        if (hasText(rq.getQ())) {
            String like = contains(rq.getQ());

            spec = spec.and((root, cq, cb) -> cb.or(
                    cb.like(cb.lower(root.get("firstName")), like),
                    cb.like(cb.lower(root.get("lastName")), like),
                    cb.like(cb.lower(root.get("pronouns")), like),
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("location")), like),
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("phone")), like),
                    cb.like(cb.lower(root.get("website")), like),
                    cb.like(cb.lower(root.get("about")), like),
                    cb.like(cb.lower(root.get("education")), like),
                    cb.like(cb.lower(root.get("video1")), like),
                    cb.like(cb.lower(root.get("video2")), like)
            ));
        }

        // ---------- creditText (ค้นจาก credits JSON อย่างเดียว) ----------
        if (hasText(rq.getCreditText())) {
            String rawLike = "%" + rq.getCreditText().trim() + "%";

            spec = spec.and((root, cq, cb) -> {
                // cast jsonb -> text แล้วค่อย LIKE
                var creditsText = cb.toString(root.get("credits"));
                return cb.like(creditsText, rawLike);
            });
        }

        // ---------- credit ids filters (dept / pos / skill) ----------
        spec = andCreditsJsonIdsContains(spec, "deptIds", rq.getCreditDeptIds());
        spec = andCreditsJsonIdsContains(spec, "posIds", rq.getCreditPosIds());
        spec = andCreditsJsonIdsContains(spec, "skillIds", rq.getCreditSkillIds());

        // ----- flags (Boolean) -----
        spec = andEq(spec, "privateProfile", rq.getPrivateProfile());
        spec = andEq(spec, "profileIsCompany", rq.getProfileIsCompany());
        spec = andEq(spec, "multiLang", rq.getMultiLang());
        spec = andEq(spec, "travel", rq.getTravel());
        spec = andEq(spec, "tour", rq.getTour());

        // ----- strings (LIKE) -----
        spec = andLike(spec, "firstName", rq.getFirstName());
        spec = andLike(spec, "lastName", rq.getLastName());
        spec = andLike(spec, "pronouns", rq.getPronouns());
        spec = andLike(spec, "title", rq.getTitle());
        spec = andLike(spec, "location", rq.getLocation());
        spec = andLike(spec, "email", rq.getEmail());
        spec = andLike(spec, "phone", rq.getPhone());
        spec = andLike(spec, "website", rq.getWebsite());
        spec = andLike(spec, "about", rq.getAbout());
        spec = andLike(spec, "education", rq.getEducation());
        spec = andLike(spec, "video1", rq.getVideo1());
        spec = andLike(spec, "video2", rq.getVideo2());

        // ----- lists (ANY MATCH / overlap) -----
        spec = andAnyMatch(spec, "workLocations", rq.getWorkLocations());
        spec = andAnyMatch(spec, "unions", rq.getUnions());
        spec = andAnyMatch(spec, "experience", rq.getExperience());
        spec = andAnyMatch(spec, "partners", rq.getPartners());
        spec = andAnyMatch(spec, "genders", rq.getGenders());
        spec = andAnyMatch(spec, "races", rq.getRaces());
        spec = andAnyMatch(spec, "additionals", rq.getAdditionals());

        var page = repo.findAll(spec, pageable);
        return page.map(profileService::toResponse);
    }

    // ---------- credits JSON contains ids ----------
    private static Specification<Profile> andCreditsJsonIdsContains(
            Specification<Profile> base,
            String jsonKey,
            List<Integer> ids) {

        if (ids == null || ids.isEmpty()) {
            return base;
        }

        return base.and((root, cq, cb) -> {

            List<Predicate> orPreds = new ArrayList<>();

            for (Integer id : ids) {
                if (id == null) {
                    continue;
                }

                // path: หา element ไหนก็ได้ใน array ที่มี <jsonKey> และใน array นั้นมีค่า == id
                // เช่น $[*].deptIds ? (@ == 1)
                String path = "$[*]." + jsonKey + " ? (@ == " + id + ")";

                orPreds.add(
                        cb.isTrue(
                                cb.function(
                                        "jsonb_path_exists",
                                        Boolean.class,
                                        root.get("credits"),
                                        cb.literal(path)
                                )
                        )
                );
            }

            // ถ้าใน list มีหลาย id → OR กัน (มีอันใดอันหนึ่งก็พอ)
            return cb.or(orPreds.toArray(Predicate[]::new));
        });
    }

    // ---------- helpers (ของเดิมใช้ได้เลย) ----------
    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static String contains(String s) {
        return "%" + s.trim().toLowerCase() + "%";
    }

    private static Specification<Profile> andEq(Specification<Profile> base, String field, Boolean value) {
        if (value == null) {
            return base;
        }
        return base.and((root, cq, cb) -> cb.equal(root.get(field), value));
    }

    private static Specification<Profile> andLike(Specification<Profile> base, String field, String value) {
        if (!hasText(value)) {
            return base;
        }
        String like = contains(value);
        return base.and((root, cq, cb) -> cb.like(cb.lower(root.get(field)), like));
    }

    private static Specification<Profile> andAnyMatch(Specification<Profile> base, String field, List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return base;
        }
        return base.and((root, cq, cb) -> {
            var join = root.join(field, JoinType.LEFT);
            return join.in(values);
        });
    }
}

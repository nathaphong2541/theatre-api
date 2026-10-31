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

        // "all" = every selected id must match (AND). Anything else = "relevant"
        // (match ANY, i.e. OR — the original behaviour).
        final boolean matchAll = "all".equalsIgnoreCase(rq.getMatchMode());

        // Order by relevance when the caller asked for it AND there are ids to
        // rank by. Relevance = how many of the selected profession ids a profile
        // has (more matches first). Only meaningful in "relevant" mode.
        final List<Integer> rankIds = (rq.getTitleIds() == null) ? List.of() : rq.getTitleIds();
        final boolean sortByRelevance = !matchAll
                && isRelevanceSort(pageable)
                && !rankIds.isEmpty();

        // "relevant" is a virtual sort token, NOT a real entity property — if it
        // ever reaches Spring Data it throws (PropertyReferenceException → 500).
        // So whenever the sort mentions it, strip it here: use manual relevance
        // ordering when we actually have ids to rank by, otherwise fall back to
        // newest-first. Also default an unsorted/absent pageable to newest-first.
        final boolean askedRelevance = isRelevanceSort(pageable);
        if (pageable == null) {
            pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "updatedAt"));
        } else if (pageable.getSort().isUnsorted() || askedRelevance) {
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    sortByRelevance ? Sort.unsorted() : Sort.by(Sort.Direction.DESC, "updatedAt"));
        }

        Specification<Profile> spec = Specification.where((root, cq, cb) -> {
            cq.distinct(true);
            // Rank by number of matching profession ids (desc), then newest.
            if (sortByRelevance && Long.class != cq.getResultType() && long.class != cq.getResultType()) {
                var score = relevanceScore(root, cq, cb, rankIds);
                if (score != null) {
                    cq.orderBy(cb.desc(score), cb.desc(root.get("updatedAt")));
                }
            }
            return cb.conjunction();
        });

        // ---------- free text q (ไม่แตะ credits เลย เพื่อกันปัญหา) ----------
        if (hasText(rq.getQ())) {
            String like = contains(rq.getQ());

            // NOTE: do not include "title" here — it is a List<Integer> (profession ids),
            // not a String, so lower()/like() cannot be applied to it.
            spec = spec.and((root, cq, cb) -> cb.or(
                    cb.like(cb.lower(root.get("firstName")), like),
                    cb.like(cb.lower(root.get("lastName")), like),
                    cb.like(cb.lower(root.get("pronouns")), like),
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
        // Privacy gate: the public directory must NEVER expose profiles that are
        // marked private. This is enforced unconditionally and ignores any
        // client-supplied privateProfile value so callers cannot opt back in.
        spec = spec.and((root, cq, cb) -> cb.isFalse(root.get("privateProfile")));
        spec = andEq(spec, "profileIsCompany", rq.getProfileIsCompany());
        spec = andEq(spec, "multiLang", rq.getMultiLang());
        spec = andEq(spec, "travel", rq.getTravel());
        spec = andEq(spec, "tour", rq.getTour());

        // ----- strings (LIKE) -----
        spec = andLike(spec, "firstName", rq.getFirstName());
        spec = andLike(spec, "lastName", rq.getLastName());
        spec = andLike(spec, "pronouns", rq.getPronouns());
        // "title" is a List<Integer> on the entity (profession ids) — cannot use LIKE.
        // Filter professions via the id-list filter instead if needed.
        spec = andLike(spec, "location", rq.getLocation());
        spec = andLike(spec, "email", rq.getEmail());
        spec = andLike(spec, "phone", rq.getPhone());
        spec = andLike(spec, "website", rq.getWebsite());
        spec = andLike(spec, "about", rq.getAbout());
        spec = andLike(spec, "education", rq.getEducation());
        spec = andLike(spec, "video1", rq.getVideo1());
        spec = andLike(spec, "video2", rq.getVideo2());

        // ----- JSONB id-list filters (ANY MATCH / overlap) -----
        // These fields are stored as JSONB arrays, so we match with jsonb_path_exists.
        spec = andJsonIdsContains(spec, "title", rq.getTitleIds(), matchAll);
        spec = andJsonIdsContains(spec, "workLocations", rq.getWorkLocations());
        spec = andJsonIdsContains(spec, "unions", rq.getUnions());
        spec = andJsonIdsContains(spec, "experience", rq.getExperience());
        spec = andJsonIdsContains(spec, "partners", rq.getPartners());
        spec = andJsonIdsContains(spec, "genders", rq.getGenders());
        spec = andJsonIdsContains(spec, "races", rq.getRaces());
        spec = andJsonIdsContains(spec, "additionals", rq.getAdditionals());

        var page = repo.findAll(spec, pageable);
        // Directory is a public listing: expose only sections each member made
        // public (and never the login email). Private profiles are already
        // excluded by the isFalse(privateProfile) predicate above.
        return page.map(profileService::toPublicResponse);
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

    /**
     * Match a top-level JSONB integer array column that contains ANY of the given ids.
     * e.g. field "title" stored as [16,20] matches titleIds=[16].
     */
    private static Specification<Profile> andJsonIdsContains(
            Specification<Profile> base,
            String field,
            List<Integer> ids) {
        // Default: match ANY (OR).
        return andJsonIdsContains(base, field, ids, false);
    }

    /**
     * Match a JSONB integer-array column against a list of ids.
     *   matchAll = false → the array contains ANY of the ids (OR).
     *   matchAll = true  → the array contains ALL of the ids (AND).
     */
    private static Specification<Profile> andJsonIdsContains(
            Specification<Profile> base,
            String field,
            List<Integer> ids,
            boolean matchAll) {

        if (ids == null || ids.isEmpty()) {
            return base;
        }

        return base.and((root, cq, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            for (Integer id : ids) {
                if (id == null) {
                    continue;
                }
                preds.add(cb.isTrue(idInJsonArray(root, cb, field, id)));
            }
            Predicate[] arr = preds.toArray(Predicate[]::new);
            // ALL selected ids present (AND) vs ANY present (OR).
            return matchAll ? cb.and(arr) : cb.or(arr);
        });
    }

    /** jsonb_path_exists(<field>, '$[*] ? (@ == id)') — true when the array has id. */
    private static jakarta.persistence.criteria.Expression<Boolean> idInJsonArray(
            jakarta.persistence.criteria.Root<Profile> root,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            String field,
            int id) {
        String path = "$[*] ? (@ == " + id + ")";
        return cb.function(
                "jsonb_path_exists",
                Boolean.class,
                root.get(field),
                cb.literal(path));
    }

    /** True when the caller asked to sort by "relevance". */
    private static boolean isRelevanceSort(Pageable pageable) {
        if (pageable == null || pageable.getSort().isUnsorted()) {
            return false;
        }
        for (Sort.Order o : pageable.getSort()) {
            if ("relevant".equalsIgnoreCase(o.getProperty()) || "relevance".equalsIgnoreCase(o.getProperty())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Relevance score = number of the selected profession ids present on the
     * profile's `title` array. Built as SUM(CASE WHEN id-present THEN 1 ELSE 0).
     */
    private static jakarta.persistence.criteria.Expression<Integer> relevanceScore(
            jakarta.persistence.criteria.Root<Profile> root,
            jakarta.persistence.criteria.CriteriaQuery<?> cq,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            List<Integer> ids) {

        jakarta.persistence.criteria.Expression<Integer> sum = null;
        for (Integer id : ids) {
            if (id == null) {
                continue;
            }
            var present = cb.<Integer>selectCase()
                    .when(cb.isTrue(idInJsonArray(root, cb, "title", id)), cb.literal(1))
                    .otherwise(cb.literal(0));
            sum = (sum == null) ? present : cb.sum(sum, present);
        }
        return sum;
    }
}

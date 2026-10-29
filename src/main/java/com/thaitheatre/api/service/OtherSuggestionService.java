package com.thaitheatre.api.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

/**
 * Suggests free-text "Other / อื่น ๆ" values that members have already entered,
 * so a new user can pick an existing entry (consistent spelling) instead of
 * retyping. Each *_other_text column stores a comma-separated string, so we
 * split, trim, de-duplicate and return the distinct pieces.
 */
@Service
@RequiredArgsConstructor
public class OtherSuggestionService {

    @PersistenceContext
    private EntityManager em;

    /**
     * Map of the public "field" key -> the actual DB column. This allow-list is
     * the ONLY place a column name is chosen, so the value interpolated into the
     * native query can never be arbitrary user input (prevents SQL injection).
     */
    private static final Map<String, String> FIELD_COLUMN = Map.ofEntries(
            Map.entry("title", "title_other_text"),
            Map.entry("workLocations", "work_locations_other_text"),
            Map.entry("unions", "union_other_text"),
            Map.entry("experience", "experience_other_text"),
            Map.entry("specialSkills", "special_skills_other_text"),
            Map.entry("partners", "partner_other_text"),
            Map.entry("genders", "gender_self_describe_text"),
            Map.entry("races", "racial_identity_other_text"));

    /** Field keys the client may ask for (for a friendly error / discovery). */
    public static java.util.Set<String> allowedFields() {
        return FIELD_COLUMN.keySet();
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<String> suggestions(String field) {
        String column = FIELD_COLUMN.get(field);
        if (column == null) {
            return List.of();
        }
        // `column` comes only from the allow-list above — safe to interpolate.
        String sql = "SELECT DISTINCT btrim(piece) AS v "
                + "FROM profiles p, "
                + "     unnest(string_to_array(COALESCE(p." + column + ", ''), ',')) AS piece "
                + "WHERE p.del_flag = 'N' AND p.record_status = 'A' "
                + "  AND btrim(piece) <> '' "
                + "ORDER BY v";
        return em.createNativeQuery(sql).getResultList();
    }
}

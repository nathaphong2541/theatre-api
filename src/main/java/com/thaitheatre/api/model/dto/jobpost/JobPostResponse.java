package com.thaitheatre.api.model.dto.jobpost;

import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Data;

/** Job post returned to clients. `createdByName` is the organization's name. */
@Data
@Builder
public class JobPostResponse {
    private Long id;
    private Long createdBy;
    private String createdByName;
    private String title;
    private String description;
    private String imageUrl;
    private List<Integer> professions;
    private List<Integer> workLocations;
    private List<Integer> experience;
    private String status;
    private boolean enabled;
    private String reviewNote;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
    /** True when today is past endDate (post no longer public). */
    private boolean expired;
    /** True when today is before startDate (scheduled, not yet public). */
    private boolean scheduled;
    private Instant createdAt;
    private Instant updatedAt;
}

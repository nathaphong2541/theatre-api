package com.thaitheatre.api.model.dto.jobpost;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Payload the organization sends to create or update a job post (JSON part). */
@Data
public class JobPostCreateRequest {
    @NotBlank
    private String title;
    private String description;
    private List<Integer> professions;
    private List<Integer> workLocations;
    private List<Integer> experience;
    /** Optional schedule. ISO date (yyyy-MM-dd). start null = now, end null = never. */
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
}

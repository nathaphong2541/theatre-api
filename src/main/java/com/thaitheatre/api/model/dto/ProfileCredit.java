package com.thaitheatre.api.model.dto;

import java.util.List;

public record ProfileCredit(
        String company,
        String title,
        String startYear,
        String endYear,
        Boolean current,
        String venue,
        String jobLocation,
        Boolean internship,
        Boolean fellowship,
        List<Integer> deptIds,
        List<Integer> posIds,
        List<Integer> skillIds) {
}
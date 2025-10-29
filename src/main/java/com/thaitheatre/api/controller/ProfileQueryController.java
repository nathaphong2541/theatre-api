package com.thaitheatre.api.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thaitheatre.api.model.dto.ProfileResponse;
import com.thaitheatre.api.model.dto.ProfileSearchRequest;
import com.thaitheatre.api.service.ProfileQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileQueryController {

    private final ProfileQueryService queryService;

    @GetMapping
    public Page<ProfileResponse> search(
            @ParameterObject ProfileSearchRequest req, // << สำคัญ
            @ParameterObject Pageable pageable // << สำคัญ
    ) {
        return queryService.search(req, pageable);
    }
}

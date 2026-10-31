package com.thaitheatre.api.controller.publice;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thaitheatre.api.model.dto.jobpost.JobPostResponse;
import com.thaitheatre.api.service.JobPostService;

import lombok.RequiredArgsConstructor;

/** Public job-post listing — only approved + enabled posts are returned. */
@RestController
@RequestMapping("/api/public/job-posts")
@RequiredArgsConstructor
public class JobPostPublicController {

    private final JobPostService service;

    @GetMapping
    public Page<JobPostResponse> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return service.listPublic(page, size);
    }

    @GetMapping("/{id}")
    public JobPostResponse get(@PathVariable Long id) {
        return service.getPublic(id);
    }
}

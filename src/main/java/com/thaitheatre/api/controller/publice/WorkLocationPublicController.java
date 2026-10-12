package com.thaitheatre.api.controller.publice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.publice.WorkLocationPublicDto;
import com.thaitheatre.api.service.publice.WorkLocationPublicService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/public/work-locations")
@RequiredArgsConstructor
public class WorkLocationPublicController {
    private final WorkLocationPublicService svc;

    @GetMapping
    public ApiPage<WorkLocationPublicDto> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return svc.list(page, size);
    }

    @GetMapping("/{id}")
    public WorkLocationPublicDto get(@PathVariable Long id) {
        return svc.get(id);
    }

    @PostMapping
    public WorkLocationPublicDto create(@RequestBody WorkLocationPublicDto dto) {
        return svc.create(dto);
    }
}

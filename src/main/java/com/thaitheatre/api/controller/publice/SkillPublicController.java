package com.thaitheatre.api.controller.publice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.publice.SkillPublicDto;
import com.thaitheatre.api.service.publice.SkillPublicService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/public/skills")
@RequiredArgsConstructor
public class SkillPublicController {
    private final SkillPublicService svc;

    @GetMapping
    public ApiPage<SkillPublicDto> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long positionId) {
        return svc.list(page, size, positionId);
    }

    @GetMapping("/{id}")
    public SkillPublicDto get(@PathVariable Long id) {
        return svc.get(id);
    }
}

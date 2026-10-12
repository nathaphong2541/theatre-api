package com.thaitheatre.api.controller.publice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.publice.UnionMembershipPublicDto;
import com.thaitheatre.api.service.publice.UnionMembershipPublicService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/public/unions")
@RequiredArgsConstructor
public class UnionMembershipPublicController {
    private final UnionMembershipPublicService svc;

    @GetMapping
    public ApiPage<UnionMembershipPublicDto> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return svc.list(page, size);
    }

    @GetMapping("/{id}")
    public UnionMembershipPublicDto get(@PathVariable Long id) {
        return svc.get(id);
    }
}

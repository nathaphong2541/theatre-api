package com.thaitheatre.api.controller.publice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.publice.PositionPublicDto;
import com.thaitheatre.api.service.publice.PositionPublicService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/public/positions")
@RequiredArgsConstructor
public class PositionPublicController {
    private final PositionPublicService svc;

    @GetMapping
    public ApiPage<PositionPublicDto> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long departmentId) {
        return svc.list(page, size, departmentId);
    }

    @GetMapping("/{id}")
    public PositionPublicDto get(@PathVariable Long id) {
        return svc.get(id);
    }

    @GetMapping("/by-department/{departmentId}")
    public ApiPage<PositionPublicDto> listByDepartment(
            @PathVariable Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return svc.listByDepartment(departmentId, page, size);
    }
}

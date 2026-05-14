package com.thaitheatre.api.controller.admin;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.dto.admin.PositionCreateUpdateDto;
import com.thaitheatre.api.model.dto.admin.PositionDto;
import com.thaitheatre.api.service.admin.PositionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/master/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService service;

    @GetMapping
    public ApiPage<PositionDto> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.list(page, size);
    }

    @GetMapping("/{id}")
    public PositionDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    public PositionDto create(@Valid @RequestBody PositionCreateUpdateDto in) {
        return service.create(in);
    }

    @PutMapping("/{id}")
    public PositionDto update(@PathVariable Long id,
            @Valid @RequestBody PositionCreateUpdateDto in) {
        return service.update(id, in);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/by-department/{departmentId}")
    public ApiPage<PositionDto> listByDepartment(
            @PathVariable Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.listByDepartment(departmentId, page, size);
    }
}

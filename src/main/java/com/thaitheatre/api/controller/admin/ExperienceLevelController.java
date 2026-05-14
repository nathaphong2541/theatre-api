package com.thaitheatre.api.controller.admin;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.dto.*;
import com.thaitheatre.api.model.dto.admin.ExperienceLevelCreateUpdateDto;
import com.thaitheatre.api.model.dto.admin.ExperienceLevelDto;
import com.thaitheatre.api.service.admin.ExperienceLevelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/master/experience-levels")
@RequiredArgsConstructor
public class ExperienceLevelController {
    private final ExperienceLevelService svc;

    @GetMapping
    public ApiPage<ExperienceLevelDto> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return svc.list(page, size);
    }

    @GetMapping("/{id}")
    public ExperienceLevelDto get(@PathVariable Long id) {
        return svc.get(id);
    }

    @PostMapping
    public ExperienceLevelDto create(@Valid @RequestBody ExperienceLevelCreateUpdateDto in) {
        return svc.create(in);
    }

    @PutMapping("/{id}")
    public ExperienceLevelDto update(@PathVariable Long id, @Valid @RequestBody ExperienceLevelCreateUpdateDto in) {
        return svc.update(id, in);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        svc.delete(id);
    }
}

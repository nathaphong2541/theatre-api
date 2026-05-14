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
import com.thaitheatre.api.model.dto.admin.ProfessionCreateUpdateDto;
import com.thaitheatre.api.model.dto.admin.ProfessionDto;
import com.thaitheatre.api.service.admin.ProfessionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/master/professions")
@RequiredArgsConstructor
public class ProfessionController {

    private final ProfessionService service;

    @GetMapping
    public ApiPage<ProfessionDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.list(page, size);
    }

    @GetMapping("/{id}")
    public ProfessionDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    public ProfessionDto create(@Valid @RequestBody ProfessionCreateUpdateDto in) {
        return service.create(in);
    }

    @PutMapping("/{id}")
    public ProfessionDto update(@PathVariable Long id,
            @Valid @RequestBody ProfessionCreateUpdateDto in) {
        return service.update(id, in);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}

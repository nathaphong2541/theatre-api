package com.thaitheatre.api.controller.admin;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.dto.admin.*;
import com.thaitheatre.api.service.admin.PersonalIdentityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/master/personal-identities")
@RequiredArgsConstructor
public class PersonalIdentityController {
    private final PersonalIdentityService svc;

    @GetMapping
    public ApiPage<PersonalIdentityDto> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return svc.list(page, size);
    }

    @GetMapping("/{id}")
    public PersonalIdentityDto get(@PathVariable Long id) {
        return svc.get(id);
    }

    @PostMapping
    public PersonalIdentityDto create(@Valid @RequestBody PersonalIdentityCreateUpdateDto in) {
        return svc.create(in);
    }

    @PutMapping("/{id}")
    public PersonalIdentityDto update(@PathVariable Long id, @Valid @RequestBody PersonalIdentityCreateUpdateDto in) {
        return svc.update(id, in);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        svc.delete(id);
    }
}

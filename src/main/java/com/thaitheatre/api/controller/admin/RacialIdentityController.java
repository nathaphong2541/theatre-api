package com.thaitheatre.api.controller.admin;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.dto.admin.*;
import com.thaitheatre.api.service.admin.RacialIdentityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/master/racial-identities")
@RequiredArgsConstructor
public class RacialIdentityController {
    private final RacialIdentityService svc;

    @GetMapping
    public ApiPage<RacialIdentityDto> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return svc.list(page, size);
    }

    @GetMapping("/{id}")
    public RacialIdentityDto get(@PathVariable Long id) {
        return svc.get(id);
    }

    @PostMapping
    public RacialIdentityDto create(@Valid @RequestBody RacialIdentityCreateUpdateDto in) {
        return svc.create(in);
    }

    @PutMapping("/{id}")
    public RacialIdentityDto update(@PathVariable Long id, @Valid @RequestBody RacialIdentityCreateUpdateDto in) {
        return svc.update(id, in);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        svc.delete(id);
    }
}

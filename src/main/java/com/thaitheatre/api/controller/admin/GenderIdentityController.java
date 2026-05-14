package com.thaitheatre.api.controller.admin;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.dto.*;
import com.thaitheatre.api.model.dto.admin.GenderIdentityCreateUpdateDto;
import com.thaitheatre.api.model.dto.admin.GenderIdentityDto;
import com.thaitheatre.api.service.admin.GenderIdentityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/master/gender-identities")
@RequiredArgsConstructor
public class GenderIdentityController {
    private final GenderIdentityService svc;

    @GetMapping
    public ApiPage<GenderIdentityDto> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return svc.list(page, size);
    }

    @GetMapping("/{id}")
    public GenderIdentityDto get(@PathVariable Long id) {
        return svc.get(id);
    }

    @PostMapping
    public GenderIdentityDto create(@Valid @RequestBody GenderIdentityCreateUpdateDto in) {
        return svc.create(in);
    }

    @PutMapping("/{id}")
    public GenderIdentityDto update(@PathVariable Long id, @Valid @RequestBody GenderIdentityCreateUpdateDto in) {
        return svc.update(id, in);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        svc.delete(id);
    }
}

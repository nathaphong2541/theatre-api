package com.thaitheatre.api.controller.admin;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.dto.admin.*;
import com.thaitheatre.api.service.admin.UnionMembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/master/unions")
@RequiredArgsConstructor
public class UnionMembershipController {
    private final UnionMembershipService svc;

    @GetMapping
    public ApiPage<UnionMembershipDto> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return svc.list(page, size);
    }

    @GetMapping("/{id}")
    public UnionMembershipDto get(@PathVariable Long id) {
        return svc.get(id);
    }

    @PostMapping
    public UnionMembershipDto create(@Valid @RequestBody UnionMembershipCreateUpdateDto in) {
        return svc.create(in);
    }

    @PutMapping("/{id}")
    public UnionMembershipDto update(@PathVariable Long id, @Valid @RequestBody UnionMembershipCreateUpdateDto in) {
        return svc.update(id, in);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        svc.delete(id);
    }
}

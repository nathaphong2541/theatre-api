package com.thaitheatre.api.controller.admin;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.dto.admin.*;
import com.thaitheatre.api.service.admin.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/master/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService svc;

    @GetMapping
    public ApiPage<SkillDto> list(@RequestParam(defaultValue="0") int page,
                                  @RequestParam(defaultValue="20") int size) {
        return svc.list(page, size);
    }

    @GetMapping("/{id}")
    public SkillDto get(@PathVariable Long id) { return svc.get(id); }

    @PostMapping
    public SkillDto create(@Valid @RequestBody SkillCreateUpdateDto in) {
        return svc.create(in);
    }

    @PutMapping("/{id}")
    public SkillDto update(@PathVariable Long id,
                           @Valid @RequestBody SkillCreateUpdateDto in) {
        return svc.update(id, in);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { svc.delete(id); }
}

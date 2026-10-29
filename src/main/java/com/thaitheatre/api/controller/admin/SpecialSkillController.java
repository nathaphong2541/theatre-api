package com.thaitheatre.api.controller.admin;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.dto.admin.SpecialSkillCreateUpdateDto;
import com.thaitheatre.api.model.dto.admin.SpecialSkillDto;
import com.thaitheatre.api.service.admin.SpecialSkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/master/special-skills")
@RequiredArgsConstructor
public class SpecialSkillController {
    private final SpecialSkillService svc;

    @GetMapping
    public ApiPage<SpecialSkillDto> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return svc.list(page, size);
    }

    @GetMapping("/{id}")
    public SpecialSkillDto get(@PathVariable Long id) {
        return svc.get(id);
    }

    @PostMapping
    public SpecialSkillDto create(@Valid @RequestBody SpecialSkillCreateUpdateDto in) {
        return svc.create(in);
    }

    @PutMapping("/{id}")
    public SpecialSkillDto update(@PathVariable Long id, @Valid @RequestBody SpecialSkillCreateUpdateDto in) {
        return svc.update(id, in);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        svc.delete(id);
    }
}

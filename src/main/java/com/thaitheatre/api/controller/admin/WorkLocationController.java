package com.thaitheatre.api.controller.admin;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.dto.admin.*;
import com.thaitheatre.api.service.admin.WorkLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/master/work-locations")
@RequiredArgsConstructor
public class WorkLocationController {

    private final WorkLocationService svc;

    @GetMapping
    public ApiPage<WorkLocationDto> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return svc.list(page, size);
    }

    @GetMapping("/{id}")
    public WorkLocationDto get(@PathVariable Long id) {
        return svc.get(id);
    }

    @PostMapping
    public WorkLocationDto create(@Valid @RequestBody WorkLocationCreateUpdateDto in) {
        return svc.create(in);
    }

    @PutMapping("/{id}")
    public WorkLocationDto update(@PathVariable Long id, @Valid @RequestBody WorkLocationCreateUpdateDto in) {
        return svc.update(id, in);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        svc.delete(id);
    }
}

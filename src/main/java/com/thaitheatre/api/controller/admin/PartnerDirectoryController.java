package com.thaitheatre.api.controller.admin;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.dto.admin.*;
import com.thaitheatre.api.service.admin.PartnerDirectoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/master/partner-directories")
@RequiredArgsConstructor
public class PartnerDirectoryController {
    private final PartnerDirectoryService svc;

    @GetMapping public ApiPage<PartnerDirectoryDto> list(@RequestParam(defaultValue="0") int page,
                                                        @RequestParam(defaultValue="20") int size){
        return svc.list(page, size);
    }
    @GetMapping("/{id}") public PartnerDirectoryDto get(@PathVariable Long id){ return svc.get(id); }
    @PostMapping public PartnerDirectoryDto create(@Valid @RequestBody PartnerDirectoryCreateUpdateDto in){ return svc.create(in); }
    @PutMapping("/{id}") public PartnerDirectoryDto update(@PathVariable Long id, @Valid @RequestBody PartnerDirectoryCreateUpdateDto in){ return svc.update(id, in); }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id){ svc.delete(id); }
}

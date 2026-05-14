// src/main/java/com/thaitheatre/api/service/PartnerDirectoryService.java
package com.thaitheatre.api.service.admin;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.dto.admin.PartnerDirectoryCreateUpdateDto;
import com.thaitheatre.api.model.dto.admin.PartnerDirectoryDto;
import com.thaitheatre.api.model.entity.admin.PartnerDirectory;
import com.thaitheatre.api.repository.admin.PartnerDirectoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PartnerDirectoryService {

    private final PartnerDirectoryRepository repo;

    public ApiPage<PartnerDirectoryDto> list(int page, int size) {
        Page<PartnerDirectory> p = repo.findAll(
                PageRequest.of(page, size, Sort.by("id").descending())
        );

        var items = p.getContent()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new ApiPage<>(
                items,
                p.getTotalElements(),
                p.getNumber(),
                p.getSize(),
                p.getTotalPages()
        );
    }

    public PartnerDirectoryDto get(Long id) {
        return toDto(req(id));
    }

    @Transactional
    public PartnerDirectoryDto create(PartnerDirectoryCreateUpdateDto in) {
        PartnerDirectory e = new PartnerDirectory();
        e.setNameTh(in.getNameTh());
        e.setNameEn(in.getNameEn());
        e.setDescription(in.getDescription());
        return toDto(repo.save(e));
    }

    @Transactional
    public PartnerDirectoryDto update(Long id, PartnerDirectoryCreateUpdateDto in) {
        PartnerDirectory e = req(id);
        e.setNameTh(in.getNameTh());
        e.setNameEn(in.getNameEn());
        e.setDescription(in.getDescription());
        return toDto(repo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }

    private PartnerDirectory req(Long id) {
        return repo.findById(id).orElseThrow();
    }

    private PartnerDirectoryDto toDto(PartnerDirectory e) {
        PartnerDirectoryDto d = new PartnerDirectoryDto();
        d.setId(e.getId());
        d.setNameTh(e.getNameTh());
        d.setNameEn(e.getNameEn());
        d.setDescription(e.getDescription());
        return d;
    }
}

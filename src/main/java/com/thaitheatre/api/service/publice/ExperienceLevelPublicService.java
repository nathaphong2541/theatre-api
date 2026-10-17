package com.thaitheatre.api.service.publice;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.entity.admin.ExperienceLevel;
import com.thaitheatre.api.model.publice.ExperienceLevelPublicDto;
import com.thaitheatre.api.repository.publice.ExperienceLevelPublicRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExperienceLevelPublicService {

    private final ExperienceLevelPublicRepository repo;

    public ApiPage<ExperienceLevelPublicDto> list(int page, int size) {
        Page<ExperienceLevel> p = repo.findByDelFlagAndRecordStatus(
                "N",
                "A",
                PageRequest.of(page, size, Sort.by("id").ascending()));

        var items = p.getContent()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new ApiPage<>(
                items,
                p.getTotalElements(),
                p.getNumber(),
                p.getSize(),
                p.getTotalPages());
    }

    public ExperienceLevelPublicDto get(Long id) {
        ExperienceLevel e = repo.findById(id).filter(x -> "N".equals(x.getDelFlag()) && "A".equals(x.getRecordStatus()))
                .orElseThrow();
        return toDto(e);
    }

    private ExperienceLevelPublicDto toDto(ExperienceLevel e) {
        ExperienceLevelPublicDto d = new ExperienceLevelPublicDto();
        d.setId(e.getId());
        d.setNameTh(e.getNameTh());
        d.setNameEn(e.getNameEn());
        d.setDescription(e.getDescription());
        return d;
    }
}

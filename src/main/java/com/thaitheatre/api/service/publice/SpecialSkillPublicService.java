package com.thaitheatre.api.service.publice;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.entity.admin.SpecialSkill;
import com.thaitheatre.api.model.publice.SpecialSkillPublicDto;
import com.thaitheatre.api.repository.publice.SpecialSkillPublicRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SpecialSkillPublicService {

    private final SpecialSkillPublicRepository repo;

    public ApiPage<SpecialSkillPublicDto> list(int page, int size) {
        Page<SpecialSkill> p = repo.findByDelFlagAndRecordStatus(
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

    public SpecialSkillPublicDto get(Long id) {
        SpecialSkill e = repo.findById(id).filter(x -> "N".equals(x.getDelFlag()) && "A".equals(x.getRecordStatus()))
                .orElseThrow();
        return toDto(e);
    }

    private SpecialSkillPublicDto toDto(SpecialSkill e) {
        SpecialSkillPublicDto d = new SpecialSkillPublicDto();
        d.setId(e.getId());
        d.setNameTh(e.getNameTh());
        d.setNameEn(e.getNameEn());
        d.setDescription(e.getDescription());
        return d;
    }
}

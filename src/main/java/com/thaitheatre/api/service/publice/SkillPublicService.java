package com.thaitheatre.api.service.publice;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.entity.admin.Skill;
import com.thaitheatre.api.model.publice.SkillPublicDto;
import com.thaitheatre.api.repository.publice.SkillPublicRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SkillPublicService {

    private final SkillPublicRepository repo;

    public ApiPage<SkillPublicDto> list(Integer page, Integer size, Long positionId) {
        Pageable pg = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Skill> p = (positionId == null)
                ? repo.findByDelFlagAndRecordStatus("N", "A", pg)
                : repo.findByDelFlagAndRecordStatusAndPosition_Id("N", "A", positionId, pg);

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

    public SkillPublicDto get(Long id) {
        Skill e = repo.findById(id).filter(x -> "N".equals(x.getDelFlag()) && "A".equals(x.getRecordStatus()))
                .orElseThrow();
        return toDto(e);
    }

    private SkillPublicDto toDto(Skill e) {
        SkillPublicDto d = new SkillPublicDto();
        d.setId(e.getId());
        d.setNameTh(e.getNameTh());
        d.setNameEn(e.getNameEn());
        d.setDescription(e.getDescription());
        d.setPositionId(e.getPosition().getId());
        d.setPositionNameTh(e.getPosition().getNameTh());
        d.setPositionNameEn(e.getPosition().getNameEn());
        return d;
    }
}

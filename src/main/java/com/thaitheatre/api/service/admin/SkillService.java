package com.thaitheatre.api.service.admin;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.dto.admin.SkillCreateUpdateDto;
import com.thaitheatre.api.model.dto.admin.SkillDto;
import com.thaitheatre.api.model.entity.admin.Position;
import com.thaitheatre.api.model.entity.admin.Skill;
import com.thaitheatre.api.repository.admin.PositionRepository;
import com.thaitheatre.api.repository.admin.SkillRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository repo;
    private final PositionRepository positionRepo;

    public ApiPage<SkillDto> list(int page, int size) {
        Page<Skill> p = repo.findAll(
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

    public SkillDto get(Long id) {
        return toDto(req(id));
    }

    @Transactional
    public SkillDto create(SkillCreateUpdateDto in) {
        Position pos = positionRepo.findById(in.getPositionId()).orElseThrow();
        Skill e = new Skill();
        e.setNameTh(in.getNameTh());
        e.setNameEn(in.getNameEn());
        e.setDescription(in.getDescription());
        e.setPosition(pos);
        return toDto(repo.save(e));
    }

    @Transactional
    public SkillDto update(Long id, SkillCreateUpdateDto in) {
        Skill e = req(id);
        Position pos = positionRepo.findById(in.getPositionId()).orElseThrow();
        e.setNameTh(in.getNameTh());
        e.setNameEn(in.getNameEn());
        e.setDescription(in.getDescription());
        e.setPosition(pos);
        return toDto(repo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }

    private Skill req(Long id) {
        return repo.findById(id).orElseThrow();
    }

    private SkillDto toDto(Skill e) {
        SkillDto dto = new SkillDto();
        dto.setId(e.getId());
        dto.setNameTh(e.getNameTh());
        dto.setNameEn(e.getNameEn());
        dto.setDescription(e.getDescription());
        dto.setPositionId(e.getPosition().getId());
        dto.setPositionNameTh(e.getPosition().getNameTh());
        dto.setPositionNameEn(e.getPosition().getNameEn());
        return dto;
    }
}

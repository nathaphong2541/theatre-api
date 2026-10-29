package com.thaitheatre.api.service.admin;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.dto.admin.SpecialSkillCreateUpdateDto;
import com.thaitheatre.api.model.dto.admin.SpecialSkillDto;
import com.thaitheatre.api.model.entity.admin.SpecialSkill;
import com.thaitheatre.api.repository.admin.SpecialSkillRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SpecialSkillService {

    private final SpecialSkillRepository repo;

    public ApiPage<SpecialSkillDto> list(int page, int size) {
        Page<SpecialSkill> p = repo.findAll(
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

    public SpecialSkillDto get(Long id) {
        return toDto(req(id));
    }

    @Transactional
    public SpecialSkillDto create(SpecialSkillCreateUpdateDto in) {
        SpecialSkill e = new SpecialSkill();
        e.setNameTh(in.getNameTh());
        e.setNameEn(in.getNameEn());
        e.setDescription(in.getDescription());
        return toDto(repo.save(e));
    }

    @Transactional
    public SpecialSkillDto update(Long id, SpecialSkillCreateUpdateDto in) {
        SpecialSkill e = req(id);
        e.setNameTh(in.getNameTh());
        e.setNameEn(in.getNameEn());
        e.setDescription(in.getDescription());
        return toDto(repo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }

    private SpecialSkill req(Long id) {
        return repo.findById(id).orElseThrow();
    }

    private SpecialSkillDto toDto(SpecialSkill e) {
        SpecialSkillDto d = new SpecialSkillDto();
        d.setId(e.getId());
        d.setNameTh(e.getNameTh());
        d.setNameEn(e.getNameEn());
        d.setDescription(e.getDescription());
        return d;
    }
}

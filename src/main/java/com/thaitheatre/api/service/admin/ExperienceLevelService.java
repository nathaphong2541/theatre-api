package com.thaitheatre.api.service.admin;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.dto.admin.ExperienceLevelCreateUpdateDto;
import com.thaitheatre.api.model.dto.admin.ExperienceLevelDto;
import com.thaitheatre.api.model.entity.admin.ExperienceLevel;
import com.thaitheatre.api.repository.admin.ExperienceLevelRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExperienceLevelService {

    private final ExperienceLevelRepository repo;

    public ApiPage<ExperienceLevelDto> list(int page, int size) {
        Page<ExperienceLevel> p = repo.findAll(
                PageRequest.of(page, size, Sort.by("id").descending()));

        var items = p.getContent()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new ApiPage<>(
                items,
                p.getTotalElements(),
                p.getNumber(), // page ปัจจุบัน
                p.getSize(), // size ต่อหน้า
                p.getTotalPages() // จำนวนหน้าทั้งหมด
        );
    }

    public ExperienceLevelDto get(Long id) {
        return toDto(req(id));
    }

    @Transactional
    public ExperienceLevelDto create(ExperienceLevelCreateUpdateDto in) {
        ExperienceLevel e = new ExperienceLevel();
        e.setNameTh(in.getNameTh());
        e.setNameEn(in.getNameEn());
        e.setDescription(in.getDescription());
        return toDto(repo.save(e));
    }

    @Transactional
    public ExperienceLevelDto update(Long id, ExperienceLevelCreateUpdateDto in) {
        ExperienceLevel e = req(id);
        e.setNameTh(in.getNameTh());
        e.setNameEn(in.getNameEn());
        e.setDescription(in.getDescription());
        return toDto(repo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }

    private ExperienceLevel req(Long id) {
        return repo.findById(id).orElseThrow();
    }

    private ExperienceLevelDto toDto(ExperienceLevel e) {
        ExperienceLevelDto d = new ExperienceLevelDto();
        d.setId(e.getId());
        d.setNameTh(e.getNameTh());
        d.setNameEn(e.getNameEn());
        d.setDescription(e.getDescription());
        return d;
    }
}

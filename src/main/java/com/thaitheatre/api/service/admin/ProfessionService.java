package com.thaitheatre.api.service.admin;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.dto.admin.ProfessionCreateUpdateDto;
import com.thaitheatre.api.model.dto.admin.ProfessionDto;
import com.thaitheatre.api.model.entity.admin.Profession;
import com.thaitheatre.api.repository.admin.ProfessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfessionService {

    private final ProfessionRepository repo;

    public ApiPage<ProfessionDto> list(int page, int size) {
        Page<Profession> p = repo.findAll(
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

    public ProfessionDto get(Long id) {
        return toDto(req(id));
    }

    @Transactional
    public ProfessionDto create(ProfessionCreateUpdateDto in) {
        Profession e = new Profession();
        e.setNameTh(in.getNameTh());
        e.setNameEn(in.getNameEn());
        e.setDescription(in.getDescription());
        return toDto(repo.save(e));
    }

    @Transactional
    public ProfessionDto update(Long id, ProfessionCreateUpdateDto in) {
        Profession e = req(id);
        e.setNameTh(in.getNameTh());
        e.setNameEn(in.getNameEn());
        e.setDescription(in.getDescription());
        return toDto(repo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }

    // helpers
    private Profession req(Long id) {
        return repo.findById(id).orElseThrow();
    }

    private ProfessionDto toDto(Profession e) {
        ProfessionDto d = new ProfessionDto();
        d.setId(e.getId());
        d.setNameTh(e.getNameTh());
        d.setNameEn(e.getNameEn());
        d.setDescription(e.getDescription());
        return d;
    }
}

package com.thaitheatre.api.service.admin;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.dto.admin.GenderIdentityCreateUpdateDto;
import com.thaitheatre.api.model.dto.admin.GenderIdentityDto;
import com.thaitheatre.api.model.entity.admin.GenderIdentity;
import com.thaitheatre.api.repository.admin.GenderIdentityRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GenderIdentityService {

    private final GenderIdentityRepository repo;

    public ApiPage<GenderIdentityDto> list(int page, int size) {
        Page<GenderIdentity> p = repo.findAll(
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

    public GenderIdentityDto get(Long id) {
        return toDto(req(id));
    }

    @Transactional
    public GenderIdentityDto create(GenderIdentityCreateUpdateDto in) {
        GenderIdentity e = new GenderIdentity();
        e.setNameTh(in.getNameTh());
        e.setNameEn(in.getNameEn());
        e.setDescription(in.getDescription());
        return toDto(repo.save(e));
    }

    @Transactional
    public GenderIdentityDto update(Long id, GenderIdentityCreateUpdateDto in) {
        GenderIdentity e = req(id);
        e.setNameTh(in.getNameTh());
        e.setNameEn(in.getNameEn());
        e.setDescription(in.getDescription());
        return toDto(repo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }

    private GenderIdentity req(Long id) {
        return repo.findById(id).orElseThrow();
    }

    private GenderIdentityDto toDto(GenderIdentity e) {
        GenderIdentityDto d = new GenderIdentityDto();
        d.setId(e.getId());
        d.setNameTh(e.getNameTh());
        d.setNameEn(e.getNameEn());
        d.setDescription(e.getDescription());
        return d;
    }
}

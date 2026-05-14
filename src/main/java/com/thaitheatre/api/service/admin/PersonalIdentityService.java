package com.thaitheatre.api.service.admin;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.dto.admin.PersonalIdentityCreateUpdateDto;
import com.thaitheatre.api.model.dto.admin.PersonalIdentityDto;
import com.thaitheatre.api.model.entity.admin.PersonalIdentity;
import com.thaitheatre.api.repository.admin.PersonalIdentityRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PersonalIdentityService {

    private final PersonalIdentityRepository repo;

    public ApiPage<PersonalIdentityDto> list(int page, int size) {
        Page<PersonalIdentity> p = repo.findAll(
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

    public PersonalIdentityDto get(Long id) {
        return toDto(req(id));
    }

    @Transactional
    public PersonalIdentityDto create(PersonalIdentityCreateUpdateDto in) {
        PersonalIdentity e = new PersonalIdentity();
        e.setNameTh(in.getNameTh());
        e.setNameEn(in.getNameEn());
        e.setDescription(in.getDescription());
        return toDto(repo.save(e));
    }

    @Transactional
    public PersonalIdentityDto update(Long id, PersonalIdentityCreateUpdateDto in) {
        PersonalIdentity e = req(id);
        e.setNameTh(in.getNameTh());
        e.setNameEn(in.getNameEn());
        e.setDescription(in.getDescription());
        return toDto(repo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }

    private PersonalIdentity req(Long id) {
        return repo.findById(id).orElseThrow();
    }

    private PersonalIdentityDto toDto(PersonalIdentity e) {
        PersonalIdentityDto d = new PersonalIdentityDto();
        d.setId(e.getId());
        d.setNameTh(e.getNameTh());
        d.setNameEn(e.getNameEn());
        d.setDescription(e.getDescription());
        return d;
    }
}

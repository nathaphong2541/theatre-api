package com.thaitheatre.api.service.publice;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.entity.admin.PersonalIdentity;
import com.thaitheatre.api.model.publice.PersonalIdentityPublicDto;
import com.thaitheatre.api.repository.publice.PersonalIdentityPublicRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PersonalIdentityPublicService {

    private final PersonalIdentityPublicRepository repo;

    public ApiPage<PersonalIdentityPublicDto> list(int page, int size) {
        Page<PersonalIdentity> p = repo.findByDelFlagAndRecordStatus(
                "N",
                "A",
                PageRequest.of(page, size, Sort.by("id").descending()));

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

    public PersonalIdentityPublicDto get(Long id) {
        PersonalIdentity e = repo.findById(id)
                .filter(x -> "N".equals(x.getDelFlag()) && "A".equals(x.getRecordStatus())).orElseThrow();
        return toDto(e);
    }

    private PersonalIdentityPublicDto toDto(PersonalIdentity e) {
        PersonalIdentityPublicDto d = new PersonalIdentityPublicDto();
        d.setId(e.getId());
        d.setNameTh(e.getNameTh());
        d.setNameEn(e.getNameEn());
        d.setDescription(e.getDescription());
        return d;
    }
}

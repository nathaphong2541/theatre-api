package com.thaitheatre.api.service.admin;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.dto.admin.WorkLocationCreateUpdateDto;
import com.thaitheatre.api.model.dto.admin.WorkLocationDto;
import com.thaitheatre.api.model.entity.admin.WorkLocation;
import com.thaitheatre.api.repository.admin.WorkLocationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkLocationService {

    private final WorkLocationRepository repo;

    public ApiPage<WorkLocationDto> list(int page, int size) {
        Page<WorkLocation> p = repo.findAll(
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

    public WorkLocationDto get(Long id) {
        return toDto(req(id));
    }

    @Transactional
    public WorkLocationDto create(WorkLocationCreateUpdateDto in) {
        WorkLocation e = new WorkLocation();
        e.setNameTh(in.getNameTh());
        e.setNameEn(in.getNameEn());
        e.setDescription(in.getDescription());
        return toDto(repo.save(e));
    }

    @Transactional
    public WorkLocationDto update(Long id, WorkLocationCreateUpdateDto in) {
        WorkLocation e = req(id);
        e.setNameTh(in.getNameTh());
        e.setNameEn(in.getNameEn());
        e.setDescription(in.getDescription());
        return toDto(repo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }

    private WorkLocation req(Long id) {
        return repo.findById(id).orElseThrow();
    }

    private WorkLocationDto toDto(WorkLocation e) {
        WorkLocationDto d = new WorkLocationDto();
        d.setId(e.getId());
        d.setNameTh(e.getNameTh());
        d.setNameEn(e.getNameEn());
        d.setDescription(e.getDescription());
        return d;
    }
}

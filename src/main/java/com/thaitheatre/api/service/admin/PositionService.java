package com.thaitheatre.api.service.admin;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.dto.admin.PositionCreateUpdateDto;
import com.thaitheatre.api.model.dto.admin.PositionDto;
import com.thaitheatre.api.model.entity.admin.Department;
import com.thaitheatre.api.model.entity.admin.Position;
import com.thaitheatre.api.repository.admin.DepartmentRepository;
import com.thaitheatre.api.repository.admin.PositionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository repo;
    private final DepartmentRepository deptRepo;

    public ApiPage<PositionDto> list(int page, int size) {
        Page<Position> p = repo.findAll(
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

    public PositionDto get(Long id) {
        return toDto(req(id));
    }

    @Transactional
    public PositionDto create(PositionCreateUpdateDto in) {
        Department dep = deptRepo.findById(in.getDepartmentId()).orElseThrow();
        Position e = new Position();
        e.setNameTh(in.getNameTh());
        e.setNameEn(in.getNameEn());
        e.setDescription(in.getDescription());
        e.setDepartment(dep);
        return toDto(repo.save(e));
    }

    @Transactional
    public PositionDto update(Long id, PositionCreateUpdateDto in) {
        Position e = req(id);
        Department d = deptRepo.findById(in.getDepartmentId()).orElseThrow();
        e.setNameTh(in.getNameTh());
        e.setNameEn(in.getNameEn());
        e.setDescription(in.getDescription());
        e.setDepartment(d);
        return toDto(repo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }

    private Position req(Long id) {
        return repo.findById(id).orElseThrow();
    }

    private PositionDto toDto(Position e) {
        PositionDto dto = new PositionDto();
        dto.setId(e.getId());
        dto.setNameTh(e.getNameTh());
        dto.setNameEn(e.getNameEn());
        dto.setDescription(e.getDescription());
        dto.setDepartmentId(e.getDepartment().getId());
        dto.setDepartmentNameTh(e.getDepartment().getNameTh());
        dto.setDepartmentNameEn(e.getDepartment().getNameEn());
        return dto;
    }

    public ApiPage<PositionDto> listByDepartment(Long departmentId, int page, int size) {

        Page<Position> p = repo.findByDepartment_Id(
                departmentId,
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
}

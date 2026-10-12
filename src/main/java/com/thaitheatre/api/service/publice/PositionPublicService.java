package com.thaitheatre.api.service.publice;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.entity.admin.Position;
import com.thaitheatre.api.model.publice.PositionPublicDto;
import com.thaitheatre.api.repository.publice.PositionPublicRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PositionPublicService {

    private final PositionPublicRepository repo;

    public ApiPage<PositionPublicDto> list(Integer page, Integer size, Long departmentId) {
        Pageable pg = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Position> p = (departmentId == null)
                ? repo.findByDelFlagAndRecordStatus("N", "A", pg)
                : repo.findByDelFlagAndRecordStatusAndDepartment_Id("N", "A", departmentId, pg);

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

    public PositionPublicDto get(Long id) {
        Position e = repo.findById(id).filter(x -> "N".equals(x.getDelFlag()) && "A".equals(x.getRecordStatus()))
                .orElseThrow();
        return toDto(e);
    }

    public ApiPage<PositionPublicDto> listByDepartment(Long departmentId, int page, int size) {

        Page<Position> p = repo.findByDepartment_Id(
                departmentId,
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

    private PositionPublicDto toDto(Position e) {
        PositionPublicDto d = new PositionPublicDto();
        d.setId(e.getId());
        d.setNameTh(e.getNameTh());
        d.setNameEn(e.getNameEn());
        d.setDescription(e.getDescription());
        d.setDepartmentId(e.getDepartment().getId());
        d.setDepartmentNameTh(e.getDepartment().getNameTh());
        d.setDepartmentNameEn(e.getDepartment().getNameEn());
        return d;
    }
}

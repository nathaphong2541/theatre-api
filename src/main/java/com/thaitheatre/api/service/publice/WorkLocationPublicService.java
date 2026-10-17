package com.thaitheatre.api.service.publice;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.entity.admin.WorkLocation;
import com.thaitheatre.api.model.publice.WorkLocationPublicDto;
import com.thaitheatre.api.repository.publice.WorkLocationPublicRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkLocationPublicService {

    private final WorkLocationPublicRepository repo;

    public ApiPage<WorkLocationPublicDto> list(int page, int size) {
        Page<WorkLocation> p = repo.findByDelFlagAndRecordStatus(
                "N",
                "A",
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

    public WorkLocationPublicDto get(Long id) {
        WorkLocation e = repo.findById(id).filter(x -> "N".equals(x.getDelFlag()) && "A".equals(x.getRecordStatus()))
                .orElseThrow();
        return toDto(e);
    }

    private WorkLocationPublicDto toDto(WorkLocation e) {
        WorkLocationPublicDto d = new WorkLocationPublicDto();
        d.setId(e.getId());
        d.setNameTh(e.getNameTh());
        d.setNameEn(e.getNameEn());
        d.setDescription(e.getDescription());
        return d;
    }

    public WorkLocationPublicDto create(WorkLocationPublicDto dto) {
        WorkLocation e = new WorkLocation();
        e.setNameTh(dto.getNameTh());
        e.setNameEn(dto.getNameEn());
        e.setDescription(dto.getDescription());

        // ค่า default ตามมาตรฐานที่คุณใช้
        e.setDelFlag("N");
        e.setRecordStatus("A");

        WorkLocation saved = repo.save(e);
        return toDto(saved);
    }
}

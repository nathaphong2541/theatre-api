package com.thaitheatre.api.service.publice;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.thaitheatre.api.common.ApiPage;
import com.thaitheatre.api.model.entity.admin.PartnerDirectory;
import com.thaitheatre.api.model.publice.PartnerDirectoryPublicDto;
import com.thaitheatre.api.repository.publice.PartnerDirectoryPublicRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PartnerDirectoryPublicService {

    private final PartnerDirectoryPublicRepository repo;

    public ApiPage<PartnerDirectoryPublicDto> list(int page, int size) {
        Page<PartnerDirectory> p = repo.findByDelFlagAndRecordStatus(
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

    public PartnerDirectoryPublicDto get(Long id) {
        PartnerDirectory e = repo.findById(id)
                .filter(x -> "N".equals(x.getDelFlag()) && "A".equals(x.getRecordStatus())).orElseThrow();
        return toDto(e);
    }

    private PartnerDirectoryPublicDto toDto(PartnerDirectory e) {
        PartnerDirectoryPublicDto d = new PartnerDirectoryPublicDto();
        d.setId(e.getId());
        d.setNameTh(e.getNameTh());
        d.setNameEn(e.getNameEn());
        d.setDescription(e.getDescription());
        return d;
    }
}

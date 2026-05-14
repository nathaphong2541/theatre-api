package com.thaitheatre.api.model.dto.admin;

import lombok.Data;

@Data
public class SkillDto {
    private Long id;
    private String nameTh;
    private String nameEn;
    private String description;
    private Long positionId;
    private String positionNameTh;
    private String positionNameEn;
}
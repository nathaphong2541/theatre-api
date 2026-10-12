package com.thaitheatre.api.model.publice;

import lombok.Data;

@Data
public class SkillPublicDto {
    private Long id;
    private String nameTh;
    private String nameEn;
    private String description;
    private Long positionId;
    private String positionNameTh;
    private String positionNameEn;
}

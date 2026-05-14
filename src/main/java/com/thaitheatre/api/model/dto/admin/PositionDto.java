package com.thaitheatre.api.model.dto.admin;

import lombok.Data;

@Data
public class PositionDto {
    private Long id;
    private String nameTh;
    private String nameEn;
    private String description;
    private Long departmentId;
    private String departmentNameTh;
    private String departmentNameEn;
}
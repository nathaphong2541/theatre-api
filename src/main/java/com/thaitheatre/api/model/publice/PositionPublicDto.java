package com.thaitheatre.api.model.publice;

import lombok.Data;

@Data
public class PositionPublicDto {
    private Long id;
    private String nameTh;
    private String nameEn;
    private String description;
    private Long departmentId;
    private String departmentNameTh;
    private String departmentNameEn;
}

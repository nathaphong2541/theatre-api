package com.thaitheatre.api.model.dto.admin;

import lombok.Data;

@Data
public class GenderIdentityDto {
    private Long id;
    private String nameTh;
    private String nameEn;
    private String description;
}

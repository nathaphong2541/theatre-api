package com.thaitheatre.api.model.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentDto {
    private Long id;
    private String nameTh;
    private String nameEn;
    private String description;
}


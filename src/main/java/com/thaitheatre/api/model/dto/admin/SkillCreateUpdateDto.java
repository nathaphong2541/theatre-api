package com.thaitheatre.api.model.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SkillCreateUpdateDto {
    @NotBlank
    private String nameTh;
    @NotBlank
    private String nameEn;
    private String description;
    @NotNull
    private Long positionId;
}
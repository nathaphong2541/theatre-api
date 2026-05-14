package com.thaitheatre.api.model.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenderIdentityCreateUpdateDto {
    @NotBlank
    private String nameTh;
    @NotBlank
    private String nameEn;
    private String description;
}

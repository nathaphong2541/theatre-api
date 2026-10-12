package com.thaitheatre.api.model.publice;

import lombok.Data;

@Data
public class PersonalIdentityPublicDto {
    private Long id;
    private String nameTh;
    private String nameEn;
    private String description;
}

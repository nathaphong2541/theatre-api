package com.thaitheatre.api.model.entity.admin;

import com.thaitheatre.api.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;

@Entity
@Table(name = "work_location",
       indexes = {
         @Index(name="idx_work_location_name_th", columnList="name_th"),
         @Index(name="idx_work_location_name_en", columnList="name_en")
       })
@Getter @Setter
public class WorkLocation extends BaseEntity {
    @Column(name="name_th", nullable=false, length=200) private String nameTh;
    @Column(name="name_en", nullable=false, length=200) private String nameEn;
    @Column(length = 500) private String description;
}

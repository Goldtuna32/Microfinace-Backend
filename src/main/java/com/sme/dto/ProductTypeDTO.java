package com.sme.dto;

import com.sme.annotation.StatusConverter;
import com.sme.entity.Status;
import lombok.Data;

@Data
public class ProductTypeDTO {
    private Long id;
    private String name;
    @StatusConverter
    private Integer status;

}

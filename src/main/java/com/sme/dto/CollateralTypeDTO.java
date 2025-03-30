package com.sme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollateralTypeDTO {
    private Long id;
    private String name;
    private Integer status;
}
package com.sme.dto;

import lombok.Data;

@Data
public class AddressDTO {
    private Long id;
    private String region;
    private String district;
    private String township;
    private String street;
}

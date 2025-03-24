package com.sme.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class PermissionDTO {
    private Long id;
    private String name;
    private String permissionFunction;
    private String description;
}
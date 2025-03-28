package com.sme.dto;

import lombok.Data;

@Data
public class RolePermissionDTO {
    private Long id;
    private RoleDTO role;
    private PermissionDTO permission;
}
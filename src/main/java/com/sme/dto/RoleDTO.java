package com.sme.dto;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class RoleDTO {
    private Long id;
    private String name;
    private String description;
    private List<PermissionDTO> permissions;
}

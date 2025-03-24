package com.sme.dto;

import lombok.Data;
import java.util.Date;

@Data
public class RoleDTO {
    private Long id;
    private String name;
    private String description;
    private Date createdAt;
    private Integer status;
}

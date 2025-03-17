package com.sme.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
    private Date dob;
    private String profilePicture; // URL from Cloudinary
    private Integer status;
    private Long roleId;
    private Long branchId;
    private LocalDateTime lastLogin;
}

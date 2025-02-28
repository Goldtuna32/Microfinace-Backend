package com.sme.dto;

import com.sme.annotation.StatusConverter;
import com.sme.entity.Status;
import lombok.Data;
import com.sme.dto.AddressDTO;
import java.util.Date;

@Data
public class BranchDTO {
    private Long id;
    private String branchName;
    private String branchCode;
    private String phoneNumber;
    private String email;
    private Date createdDate;
    private Date updatedDate;
    @StatusConverter
    private Integer status;
    private AddressDTO address;

}

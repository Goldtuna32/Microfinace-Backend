package com.sme.dto;

import com.sme.annotation.StatusConverter;
import com.sme.entity.Status;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DealerRegistrationDTO {
    private Long id;
    private String companyName;
    private String phoneNumber;
    private LocalDateTime registrationDate;
    @StatusConverter
    private Integer status;
    private int addressId;
    private int currentAccountId;


}

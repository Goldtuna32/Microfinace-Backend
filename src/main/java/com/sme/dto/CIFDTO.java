package com.sme.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sme.annotation.StatusConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CIFDTO {
    private Long id;
    private String name;
    private String nrcNumber;
    private LocalDate dob;
    private String gender;
    private String phoneNumber;
    private String email;
    private String address;
    private String maritalStatus;
    private String occupation;
    private String incomeSource;
    private String serialNumber;
    @StatusConverter
    private Integer status;
    private LocalDateTime createdAt;
    private Long branchId;  // Reference Branch ID

    private boolean hasCurrentAccount;

    private MultipartFile frontNrc;
    private MultipartFile backNrc;


    @JsonProperty("fNrcPhotoUrl")
    private String fNrcPhotoUrl;
    @JsonProperty("bNrcPhotoUrl")
    private String bNrcPhotoUrl;
}

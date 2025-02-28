package com.sme.dto;

import com.sme.annotation.StatusConverter;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class CollateralDTO {
    private Long id;
    private BigDecimal value;
    private String description;
    private String f_collateral_photo;
    private String b_collateral_photo;
    @StatusConverter
    private Integer status;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;
    private String collateralCode;
    private Long cifId;
    private Long collateralTypeId;  // Changed back to camelCase
}

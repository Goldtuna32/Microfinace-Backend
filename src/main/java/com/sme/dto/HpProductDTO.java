package com.sme.dto;

import com.sme.annotation.StatusConverter;
import com.sme.entity.Status;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class HpProductDTO {
    private Long id;
    private String name;
    @StatusConverter
    private Integer status;
    private BigDecimal price;
    private Long productTypeId;
    private Long dealerRegistrationId;
    private BigDecimal commissionFee; // ✅ Ensure this is included
    private String hpProductPhoto;


}

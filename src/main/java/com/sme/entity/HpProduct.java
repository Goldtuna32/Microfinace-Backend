package com.sme.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sme.annotation.StatusConverter;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "hp_product")
public class HpProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @StatusConverter
    @Column(name = "status")
    private Integer status;


    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "product_type_id", nullable = false)
    private ProductType productType;

 
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "dealer_registration_id", nullable = false)
    @JsonIgnore
    private DealerRegistration dealerRegistration; // ✅ Corrected mapping

    @Column(name = "hp_registration_id", nullable = false)
    private int hpRegistrationId;

 
    @Column(name = "commission_fee", precision = 10, scale = 2, nullable = false)
    private BigDecimal commissionFee;
 

 
}

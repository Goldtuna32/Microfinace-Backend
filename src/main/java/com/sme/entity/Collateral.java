package com.sme.entity;

import com.sme.annotation.StatusConverter;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "collateral")
public class Collateral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "value", nullable = false, precision = 15, scale = 2)
    private BigDecimal value;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "f_collateral_photo", nullable = false, length = 255)
    private String f_collateral_photo;

    @Column(name = "b_collateral_photo", nullable = false, length = 255)
    private String b_collateral_photo;

    @StatusConverter
    @Column(nullable = false, length = 45)
    private Integer status;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date date;

    @Column(name = "collateral_code", nullable = false, length = 45, unique = true)
    private String collateralCode;

    @ManyToOne
    @JoinColumn(name = "cif_id", nullable = false)
    private CIF cif;
    @ManyToOne
    @JoinColumn(name = "collateral_type_id", nullable = false) // Changed from collateral_type to collateral_type_id
    private CollateralType collateralType;

    @OneToMany(mappedBy = "collateral", cascade = CascadeType.ALL)
    private List<SmeLoanCollateral> smeLoanCollaterals;
}

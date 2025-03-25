package com.sme.entity;

import com.sme.annotation.StatusConverter;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;


@Entity
@Table(name = "collateral_type")
@Data
public class CollateralType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @StatusConverter
    @Column(nullable = false, length = 45)
    private Integer status;
}

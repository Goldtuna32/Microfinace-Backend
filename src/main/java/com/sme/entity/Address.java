package com.sme .entity;

import com.sme.entity.Branch;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 45, nullable = false)
    private String region;

    @Column(length = 45, nullable = false)
    private String district;

    @Column(length = 45, nullable = false)
    private String township;

    @Column(length = 45, nullable = false)
    private String street;

    @OneToOne(mappedBy = "address", cascade = CascadeType.ALL, orphanRemoval = true)
    private Branch branch;

}

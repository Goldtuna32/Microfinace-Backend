package com.sme.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sme.annotation.StatusConverter;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "cif")
public class CIF {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String serialNumber;

    @Column(name = "name", nullable = false, length = 60)
    private String name;

    @Column(name = "nrc_number", nullable = false, length = 45)
    private String nrcNumber;

    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Column(name = "gender", nullable = false, length = 45)
    private String gender;

    @Column(name = "phone_number", nullable = false, length = 45)
    private String phoneNumber;

    @Column(name = "email", nullable = false, length = 45)
    private String email;

    @Column(name = "address", nullable = false, length = 45)
    private String address;

    @Column(name = "martial_status", nullable = false, length = 45)
    private String maritalStatus;

    @Column(name = "occupation", nullable = false, length = 45)
    private String occupation;

    @Column(name = "income_source", nullable = false, length = 45)
    private String incomeSource;


    @StatusConverter
    @Column(nullable = false, length = 45)
    private Integer status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "f_nrc_photo", nullable = false, length = 255)
    private String fNrcPhotoUrl;

    @Column(name = "b_nrc_photo", nullable = false, length = 255)
    private String bNrcPhotoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @OneToOne(mappedBy = "cif", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CurrentAccount currentAccount;

    @OneToMany(mappedBy = "cif", cascade = CascadeType.ALL)
    private List<Collateral> collaterals;

}

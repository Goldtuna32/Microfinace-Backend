package com.sme.entity;

import com.sme.annotation.StatusConverter;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@Table(name = "current_account")
public class CurrentAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false, length = 45, insertable = false, updatable = false,unique = true)
    private String accountNumber;

    @Column(nullable = false, length = 45)
    private BigDecimal balance;

    @Column(name = "maximium_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal maximumBalance;

    @Column(name = "minimum_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal minimumBalance;

    @StatusConverter
    @Column(name = "account_status", nullable = false, length = 45)
    private Integer status;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_created", nullable = false)
    private Date dateCreated;

    @Column(name = "hold_amount", precision = 18, scale = 2, nullable = true)
    private BigDecimal holdAmount;

    @OneToOne
    @JoinColumn(name = "cif_id", referencedColumnName = "id")
    private CIF cif;

    @OneToMany(mappedBy = "currentAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountTransaction> transactions;

}
